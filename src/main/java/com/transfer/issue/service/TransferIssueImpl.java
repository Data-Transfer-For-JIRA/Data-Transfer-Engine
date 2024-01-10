package com.transfer.issue.service;


import com.account.dto.AdminInfoDTO;
import com.account.entity.TB_JIRA_USER_Entity;
import com.account.service.Account;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transfer.issue.model.FieldInfo;
import com.transfer.issue.model.FieldInfoCategory;
import com.transfer.issue.model.dao.PJ_PG_SUB_JpaRepository;
import com.transfer.issue.model.dto.*;
import com.transfer.issue.model.dto.weblink.CreateWebLinkDTO;
import com.transfer.issue.model.dto.weblink.RequestWeblinkDTO;
import com.transfer.issue.model.dto.weblink.SearchWebLinkDTO;
import com.transfer.issue.model.entity.PJ_PG_SUB_Entity;
import com.transfer.project.model.dao.TB_JLL_JpaRepository;
import com.transfer.project.model.dao.TB_PJT_BASE_JpaRepository;
import com.transfer.project.model.entity.TB_JLL_Entity;
import com.transfer.project.model.entity.TB_JML_Entity;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.transfer.issue.model.FieldInfo.ofLabel;

@AllArgsConstructor
@Service("transferIssue")
public class TransferIssueImpl implements TransferIssue {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Account account;

    @Autowired
    private com.transfer.project.model.dao.TB_JML_JpaRepository TB_JML_JpaRepository;

    @Autowired
    private TB_JLL_JpaRepository TB_JLL_JpaRepository;

    @Autowired
    private PJ_PG_SUB_JpaRepository PJ_PG_SUB_JpaRepository;

    @Autowired
    private com.account.dao.TB_JIRA_USER_JpaRepository TB_JIRA_USER_JpaRepository;

    @Autowired
    private TB_PJT_BASE_JpaRepository TB_PJT_BASE_JpaRepository;

    @Transactional
    @Override
    public Map<String ,String> transferIssueData(TransferIssueDTO transferIssueDTO) throws Exception {
        logger.info("[::TransferIssueImpl::] transferIssueData");
        Map<String, String> result = new HashMap<>();
        String projectCode = transferIssueDTO.getProjectCode();
        // 생성할 프로젝트 조회
        TB_JML_Entity project = checkProjectCreated(projectCode);

        if (project != null) {
            processTransferIssues(project, transferIssueDTO, result);
            return result;
        } else {
            logger.info("생성된 프로젝트가 아닙니다.");
            result.put(projectCode, "해당 프로젝트는 지라에 없습니다.");
            return result;
        }

    }
    /*
     *  지라이슈 생성 로직
     * */
    public Map<String, String> processTransferIssues(TB_JML_Entity project, TransferIssueDTO transferIssueDTO, Map<String, String> result) throws Exception {
        logger.info("[::TransferIssueImpl::] processTransferIssues");
        String projectCode = transferIssueDTO.getProjectCode();
        String jiraProjectKey = TB_JML_JpaRepository.findByProjectCode(transferIssueDTO.getProjectCode()).getKey();

        List<PJ_PG_SUB_Entity> issueList = PJ_PG_SUB_JpaRepository.findAllByProjectCodeOrderByCreationDateDesc(projectCode);

        if(issueList.isEmpty() || issueList == null){
            createBaseInfoIssue(issueList, project);
            if (checkIssueMigrateFlag(projectCode)) {
                result.put("jiraKey", jiraProjectKey);
                result.put("result", "이슈 생성 성공");
            } else {
                result.put("jiraKey", jiraProjectKey);
                result.put("result", "이슈 생성 실패");
            }
        } else {
            if(createBaseInfoIssue(issueList, project)){
                ResponseIssueDTO issue  = createWssHistoryIssue(issueList, project);
                if(issue != null){
                    System.out.println("상태변경 대상 키"+issue.getKey());
                    if (checkIssueMigrateFlag(projectCode)) {
                        result.put("jiraKey", jiraProjectKey);
                        result.put("result", "이슈 생성 성공");
                    } else {
                        result.put("jiraKey", jiraProjectKey);
                        result.put("result", "이슈 생성 실패");
                    }
                } else {
                    result.put("jiraKey", jiraProjectKey);
                    result.put("result","이슈 생성 실패");
                }
            } else {
                result.put("jiraKey", jiraProjectKey);
                result.put("result","이슈 생성 실패");
            }
        }
        return result;
    }
    /*
     *  프로젝트가 이관되어있는지 확인
     * */
    public TB_JML_Entity checkProjectCreated(String projectCode){
        logger.info("[::TransferIssueImpl::] checkProjectCreated");
       return TB_JML_JpaRepository.findByProjectCode(projectCode);
    }

    /*
     *  최초 이슈 생성
     * */
    public Boolean createBaseInfoIssue(List<PJ_PG_SUB_Entity> issueList, TB_JML_Entity migratedProject) throws Exception {
        logger.info("[::TransferIssueImpl::] createBaseInfoIssue 기본 정보 이슈 생성 시작");

        // 1. JML 테이블에서 정보 가져오기
        // 2. BASE 테이블에서 필요한 필드 값 가져오기
        // 3. jiraProjectKey를 프로젝트 키로 하여, 필요한 값 할당해서 이슈 생성

        // 이관 날짜
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedToday = today.format(formatter);

        String jiraProjectKey = migratedProject.getKey(); // 지라 프로젝트 키
        String assignees = migratedProject.getProjectAssignees(); // 담당자
        String wssProjectCode = migratedProject.getProjectCode(); // WSS 프로젝트 키
        String creationDate = null; // 프로젝트 배정일

        if (!issueList.isEmpty()) {
            creationDate = String.valueOf(issueList.get(issueList.size()-1).getCreationDate());
        }

        // 설명 설정
        String baseDescription = "\n본 이슈는 [" + formattedToday + "] WSS에서 이관한 이슈입니다.\n"
                + "기타 필드에서 상세 정보를 확인하실 수 있습니다.";


        // WSS에서 이관할 데이터를 프로젝트 타입에 맞는 DTO에 set
        TB_PJT_BASE_Entity baseInfo = TB_PJT_BASE_JpaRepository.findByProjectCode(wssProjectCode);

        CreateIssueDTO<?> createIssueDTO;
        String projectTitle = "프로젝트 기본 정보";
        String maintenanceTitle = "유지보수 기본 정보";

        // 프로젝트인지 유지보수인지 판별
        if (baseInfo.getProjectFlag().equals("P")) {

            ProjectInfoDTO.ProjectInfoDTOBuilder<?, ?> projectBuilder = ProjectInfoDTO.builder();
            projectBuilder = setCommonFields(projectBuilder, baseInfo, jiraProjectKey, baseDescription, assignees);

            // 이슈타입
            FieldInfo issueTypeFieldInfo = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, projectTitle);
            if (issueTypeFieldInfo != null) {
                projectBuilder.issuetype(new FieldDTO.Field(issueTypeFieldInfo.getId()));
            }

            // 제목
            String baseSummary = "["+ formattedToday + "] " + projectTitle;
            projectBuilder.summary(baseSummary);

            // 프로젝트명
            if (baseInfo.getProjectName() != null) {
                projectBuilder.projectName(baseInfo.getProjectName());
            }

            // 프로젝트 코드
            if (wssProjectCode != null) {
                projectBuilder.projectCode(baseInfo.getProjectCode());
            }

            // 프로젝트 배정일
            if (creationDate != null) {
                projectBuilder.projectAssignmentDate(creationDate);
            }

            // 프로젝트 진행 단계
            String projectProgressStepId = FieldInfo.getIdByCategoryAndLabel(
                    FieldInfoCategory.PROJECT_PROGRESS_STEP,
                    String.valueOf(baseInfo.getProjectStep()));
            if (projectProgressStepId != null) {
                projectBuilder.projectProgressStep(new FieldDTO.Field(projectProgressStepId));
            }

            ProjectInfoDTO projectInfoDTO = projectBuilder.build();
            createIssueDTO = new CreateIssueDTO<>(projectInfoDTO);

        } else {

            MaintenanceInfoDTO.MaintenanceInfoDTOBuilder<?, ?> maintenanceBuilder = MaintenanceInfoDTO.builder();
            maintenanceBuilder = setCommonFields(maintenanceBuilder, baseInfo, jiraProjectKey, baseDescription, assignees);

            // 이슈타입
            FieldInfo issueTypeFieldInfo = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, maintenanceTitle);
            if (issueTypeFieldInfo != null) {
                maintenanceBuilder.issuetype(new FieldDTO.Field(issueTypeFieldInfo.getId()));
            }

            // 제목
            String baseSummary = "["+ formattedToday + "] " + maintenanceTitle;
            maintenanceBuilder.summary(baseSummary);

            // 유지보수명
            if (baseInfo.getProjectName() != null) {
                maintenanceBuilder.maintenanceName(baseInfo.getProjectName());

            }

            // 유지보수 코드
            if (baseInfo.getProjectCode() != null) {
                maintenanceBuilder.maintenanceCode(baseInfo.getProjectCode());
            }

            // 계약 여부
            FieldInfo contractStatusInfo = FieldInfo.ofLabel(FieldInfoCategory.CONTRACT_STATUS, String.valueOf(baseInfo.getContract()));
            if (contractStatusInfo != null) {
                maintenanceBuilder.contractStatus(new FieldDTO.Field(contractStatusInfo.getId()));
            }

            // 유지보수 시작일
            if (baseInfo.getContractStartDate() != null) {
                maintenanceBuilder.maintenanceStartDate(String.valueOf(baseInfo.getContractStartDate()));
            }

            // 유지보수 종료일
            if (baseInfo.getContractEndDate() != null) {
                maintenanceBuilder.maintenanceEndDate(String.valueOf(baseInfo.getContractEndDate()));
            }

            MaintenanceInfoDTO maintenanceInfoDTO = maintenanceBuilder.build();
            createIssueDTO = new CreateIssueDTO<>(maintenanceInfoDTO);
        }

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());

        // 이슈 생성
        String endpoint = "/rest/api/3/issue";
        ResponseIssueDTO responseIssueDTO = null;
        try {
            responseIssueDTO = WebClientUtils.post(webClient, endpoint, createIssueDTO, ResponseIssueDTO.class).block();


        } catch (Exception e) {
            if (e instanceof WebClientResponseException) {
                WebClientResponseException wcException = (WebClientResponseException) e;
                HttpStatus status = wcException.getStatusCode();
                String body = wcException.getResponseBodyAsString();

                System.out.println("[::TransferIssueImpl::] createBaseInfoIssue -> " + status + " : " + body);
            }
        }

        // 이슈 생성되었는지 체크 / 이슈 상태를 완료됨으로 변경
        if (responseIssueDTO.getKey() != null) {
            relatedProject(wssProjectCode,responseIssueDTO.getKey());
            changeIssueStatus(responseIssueDTO.getKey());
            return true;
        } else {
            return false;
        }
    }

    /*
     *  프로젝트 담당자 이름으로 지라서버 아이디 디비 검색 리스트로 반환 
     *  2명만 선택
     * */
    public List<String> getSeveralAssigneeId(String userNames) throws Exception {
        logger.info("[::TransferIssueImpl::] getSeveralAssigneeId");

        List<String> userIdList = new ArrayList<>();
        String epageDivAccountId = TB_JIRA_USER_JpaRepository.findByDisplayName("epage div").getAccountId();

        if (userNames != null && !userNames.trim().isEmpty()) {

            if (validateAssigneeFormat(userNames)) {

                String[] namesArray = userNames.trim().split("\\s*,\\s*");
                List<String> namesArrayList = Arrays.asList(namesArray);
                List<String> limitedNamesList = namesArrayList.subList(0, Math.min(namesArrayList.size(), 2));

                for (String name : limitedNamesList) {

                    Optional<TB_JIRA_USER_Entity> userEntity = TB_JIRA_USER_JpaRepository.findByDisplayNameContaining(name)
                            .stream()
                            .findFirst();

                    userEntity.ifPresentOrElse(entity -> {
                        // userEntity가 존재하는 경우
                        String userId = entity.getAccountId();
                        if (userId != null) {
                            userIdList.add(userId);
                        }
                    }, () -> {
                        // userEntity가 존재하지 않는 경우 (퇴사자 등)
                        userIdList.add(epageDivAccountId);
                    });
                }
                return userIdList;
            }
            // 유효한 형식이 아닌 경우
            userIdList.add(epageDivAccountId);
        } else {
            // 담당자 미지정
            userIdList.add(epageDivAccountId);
        }
        return userIdList;
    }
    /*
     *  이슈 담당자 이름으로 지라서버 아이디 디비 검색
     * */
    @Override
    public String getOneAssigneeId(String userNames) throws Exception {
        logger.info("[::TransferIssueBySchedulerImpl::] getOneAssigneeId");
        String epageDivAccountId = TB_JIRA_USER_JpaRepository.findByDisplayName("epage div").getAccountId();

        if (userNames == null || userNames.trim().isEmpty()) {
            return epageDivAccountId;
        }

        String userName = Arrays.stream(userNames.split(",")).findFirst().orElse(userNames);
        List<TB_JIRA_USER_Entity> user = TB_JIRA_USER_JpaRepository.findByDisplayNameContaining(userName);
        if (!user.isEmpty()) {
            String userId = user.get(0).getAccountId();
            return userId;
        } else {
            return epageDivAccountId; // 담당자가 관리 목록에 없으면 전자문서 사업부 기본아이디로 삽입
        }
    }
    /*
     *  담당자 이름 포맷 검증
     * */
    public Boolean validateAssigneeFormat(String userNames) {
        logger.info("[::TransferIssueImpl::] validateAssigneeFormat");

        String pattern = "^([^,]+)(,([^,]+))*$";
        if (userNames.matches(pattern)) {
            return true;
        }

        return false;
    }

    /*
     *  생성한 이슈의 상태를 변환하는 메서드
     * */
    @Override
    public void changeIssueStatus(String issueKey) throws Exception {
        logger.info("[::TransferIssueImpl::] changeIssueStatus");

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint ="/rest/api/3/issue/"+issueKey+"/transitions";
        String transitionID = ofLabel(FieldInfoCategory.ISSUE_STATUS,"완료됨").getId();
        TransitionDTO transitionDTO = new TransitionDTO();
        TransitionDTO.Transition transition = TransitionDTO.Transition.builder()
                .id(transitionID)
                .build();
        transitionDTO.setTransition(transition);

        try{
            WebClientUtils.post(webClient,endpoint,transitionDTO,void.class).block();
            /*
            * WebClient는 Spring WebFlux의 일부로, 리액티브 프로그래밍 모델을 따릅니다. 이 모델에서는 데이터 스트림이 "핫" 또는 "콜드" 두 가지로 분류됩니다.
            * "핫" 스트림은 구독자가 없어도 데이터를 방출하지만, "콜드" 스트림은 구독자가 있을 때만 데이터를 방출하며, WebClient가 반환하는 Mono와 Flux는 "콜드" 스트림에 속합니다.
            * 따라서 .block()을 호출하지 않고 Mono나 Flux 객체를 생성만 하고 구독하지 않으면, 실제로 HTTP 요청이 실행되지 않습니다.
            * .block() 메서드는 구독을 실행하고, 데이터가 방출될 때까지 대기하기 때문에, 이 메서드를 호출하지 않으면 HTTP 요청이 전송되지 않습니다.
            * */

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
    /*
    *  WSS 이슈 히스토리로 이슈 생성하는 메서드
    * */
    public ResponseIssueDTO createWssHistoryIssue(List<PJ_PG_SUB_Entity> issueList,TB_JML_Entity migratedProject) throws Exception {
        logger.info("[::TransferIssueImpl::] createWssHistoryIssue");
        String jiraProjectId = migratedProject.getId();
        // wss 작성이슈 내용
        List<String> contentsList = new ArrayList<>();  // 문자열을 저장할 리스트
        String wssContents = "";
        for (PJ_PG_SUB_Entity issue : issueList){

            Date  date = issue.getCreationDate();
            String title = "작성일: " + date +"     작성자: "+issue.getWriter();
            String content = issue.getIssueContent().replace("<br>", "\n").replace("&nbsp;", " ");
            String divider = "====================================================================";
            String contentItem = title+"\n"+content+"\n\n"+divider+"\n\n";

            if ((wssContents.length() + contentItem.length()) > 30000) {
                contentsList.add(wssContents);  // 길이가 3만자를 넘어가면 리스트에 추가
                wssContents = contentItem;  // wssContents를 contentItem으로 초기화
            } else {
                wssContents += contentItem;
            }
        }
        if (!wssContents.isEmpty()) {
            contentsList.add(wssContents);  // 마지막에 남은 wssContents를 리스트에 추가
        }


        List<CreateIssueDTO> createIssueDTOList = new ArrayList<>();  // CreateIssueDTO 객체를 저장할 리스트

        for (String contentText : contentsList) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedToday = today.format(formatter);

            FieldDTO fieldDTO = new FieldDTO();

            // 프로젝트 아이디
            FieldDTO.Project project = FieldDTO.Project.builder()
                    .id(jiraProjectId)
                    .build();
            fieldDTO.setProject(project);


            // wss 이슈 제목
            String summary = "["+formattedToday+"] WSS HISTORY";
            fieldDTO.setSummary(summary);

            FieldDTO.ContentItem contentItem = FieldDTO.ContentItem.builder()
                    .type("text")
                    .text(contentText)
                    .build();
            List<FieldDTO.ContentItem> contentItems = Collections.singletonList(contentItem);

            FieldDTO.Content content = FieldDTO.Content.builder()
                    .content(contentItems)
                    .type("paragraph")
                    .build();
            List<FieldDTO.Content> contents = Collections.singletonList(content);

            FieldDTO.Description description = FieldDTO.Description.builder()
                    .version(1)
                    .type("doc")
                    .content(contents)
                    .build();

            fieldDTO.setDescription(description);

            FieldDTO.Field field = FieldDTO.Field.builder()
                    .id("10002")
                    .build();
            fieldDTO.setIssuetype(field);

            CreateIssueDTO createIssueDTO = new CreateIssueDTO<>(fieldDTO);
            createIssueDTOList.add(createIssueDTO);  // 리스트에 추가
        }

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint ="/rest/api/3/issue";

        List<ResponseIssueDTO> responseList = new ArrayList<>();

        for(CreateIssueDTO createIssueDTO : createIssueDTOList){
            ResponseIssueDTO response = WebClientUtils.postByFlux(webClient, endpoint, createIssueDTO, ResponseIssueDTO.class).blockFirst();
            changeIssueStatus(response.getKey());
            responseList.add(response);
        }

        return responseList.get(0);
    }

    public boolean checkIssueMigrateFlag(String projectCode) throws Exception{

        logger.info("[::TransferIssueImpl::] CheckMigrateFlag");
        logger.info("::TransferIssueImpl::] projectCode -> " + projectCode);
        Optional<TB_PJT_BASE_Entity> entity =  TB_PJT_BASE_JpaRepository.findById(projectCode);
        if(entity.isPresent()){
            TB_PJT_BASE_Entity updatedEntity = entity.get();
            updatedEntity.setMigrateFlag(true);
            TB_PJT_BASE_JpaRepository.save(updatedEntity);
            return true;
        }
        return false;
    }

    public void relatedProject(String projectCode, String issueIdOrKey) throws JsonProcessingException {


        if (projectCode == null || projectCode.isEmpty() || issueIdOrKey == null || issueIdOrKey.isEmpty()) {
            throw new IllegalArgumentException("프로젝트 코드 또는 이슈 ID/키가 유효하지 않습니다.");
        }

        // 프로젝트 조회해서 relatedProject 가 projectCode인 프로젝트 리스트 구하기
        List<TB_PJT_BASE_Entity> relatedProjectList = TB_PJT_BASE_JpaRepository.findByRelatedProject(projectCode);

        // 이관 여부에 따라 프로젝트를 그룹화하고, 각 그룹의 프로젝트 정보를 리스트로 모아서 반환
        Map<Boolean, List<AbstractMap.SimpleEntry<String, String>>> groupedProjects = relatedProjectList.stream()
                .map(relatedProject -> {
                    String relatedProjectCode = relatedProject.getProjectCode(); // 해당 프로젝트에 연결된 프로젝트 코드
                    String relatedProjectName = relatedProject.getProjectName();
                    TB_JML_Entity migratedProject = TB_JML_JpaRepository.findByProjectCode(relatedProjectCode); // 연결된 프로젝트 코드로 이관여부 확인
                    if (migratedProject == null) { // 이관이 안되어있으면?
                        return new AbstractMap.SimpleEntry<>(false, new AbstractMap.SimpleEntry<>(relatedProjectCode, relatedProjectName));
                    } else { // 이관이 되어있으면?
                        String title = migratedProject.getJiraProjectName();
                        return new AbstractMap.SimpleEntry<>(true, new AbstractMap.SimpleEntry<>(migratedProject.getKey() != null ? migratedProject.getKey() : relatedProjectCode, title));
                        // migratedProject.getKey()
                    }
                })
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        //  스트림의 Entry 객체들을 이관 여부를 기준으로 그룹화하고, 각 그룹의 프로젝트 정보를 List로 수집하는 Collector를 생성 ,이관 여부를 키로, 그룹의 프로젝트 정보 리스트를 값으로 가지는 Map
        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());


        String result = "";
        if (groupedProjects.containsKey(false)) {
            result += "[연관된 프로젝트 - 지라 프로젝트 이관 전]\nWSS 프로젝트 코드\n    - " +
                    String.join("\n    - ", groupedProjects.get(false).stream()
                            .map(entry -> entry.getKey() + ": " + entry.getValue())
                            .collect(Collectors.toList())) + "\n";

            AddCommentDTO addCommentDTO = new AddCommentDTO();

            AddCommentDTO.TextContent textContent = AddCommentDTO.TextContent.builder()
                    .text(result)
                    .type("text")
                    .build();

            List<AddCommentDTO.TextContent> textContentList = new ArrayList<>();
            textContentList.add(textContent);

            AddCommentDTO.Content content = AddCommentDTO.Content.builder()
                    .type("paragraph")
                    .content(textContentList)
                    .build();

            List<AddCommentDTO.Content> contentList = new ArrayList<>();
            contentList.add(content);

            AddCommentDTO.Body body = AddCommentDTO.Body.builder()
                    .version(Integer.parseInt("1"))
                    .type("doc")
                    .content(contentList)
                    .build();

            addCommentDTO.setBody(body);
            String endpoint = "/rest/api/3/issue/"+issueIdOrKey+"/comment";
            WebClientUtils.post(webClient,endpoint,addCommentDTO,String.class).block();
        }
        if (groupedProjects.containsKey(true)) { // 이관이 완료된 프로젝트 정보 리스트를 연결한 문자열 반환
            List<AbstractMap.SimpleEntry<String, String>> migratedProjectInfo = groupedProjects.get(true);
            for(AbstractMap.SimpleEntry<String, String> migratedProject : migratedProjectInfo) {
                String key = migratedProject.getKey();
                String title = migratedProject.getValue();
                String url = "https://markany.atlassian.net/jira/core/projects/" + key + "/board";

                CreateWebLinkDTO.Icon icon = CreateWebLinkDTO.Icon.builder()
                        .url16x16("https://markany.atlassian.net/favicon.ico")
                        .build();
                CreateWebLinkDTO.Object object = CreateWebLinkDTO.Object.builder()
                        .icon(icon)
                        .title(title)
                        .url(url)
                        .build();

                CreateWebLinkDTO createWebLinkDTO = new CreateWebLinkDTO();
                createWebLinkDTO.setObject(object);

                String endpoint = "/rest/api/3/issue/"+issueIdOrKey+"/remotelink";
                WebClientUtils.post(webClient,endpoint, createWebLinkDTO,String.class).block();
            }

        }

    }

    /*
     *  프로젝트와 유지보수 공통 필드 설정
     * */
    public <B extends CustomFieldDTO.CustomFieldDTOBuilder<?, ?>> B setCommonFields(B customBuilder, TB_PJT_BASE_Entity baseInfo, String jiraProjectKey, String baseDescription, String assignees) throws Exception {

        // 프로젝트
        customBuilder.project(new FieldDTO.Project(jiraProjectKey, null));

        // 설명
        customBuilder.description(setDescription(Collections.singletonList(baseDescription)));

        // 담당자 및 부 담당자
        List<String> assigneeList = getSeveralAssigneeId(assignees); // 담당자 리스트

        if (assigneeList != null) {
            if (assigneeList.size() >= 1) {
                customBuilder.assignee(new FieldDTO.User(assigneeList.get(0)));
            }
            if (assigneeList.size() == 2) {
                customBuilder.subAssignee(new FieldDTO.User(assigneeList.get(1)));
            }
        }

        // 영업 대표
        if (baseInfo.getSalesManager() != null) {
            if (getSeveralAssigneeId(baseInfo.getSalesManager()) != null && !getSeveralAssigneeId(baseInfo.getSalesManager()).isEmpty()) {
                String salesManagerId = getSeveralAssigneeId(baseInfo.getSalesManager()).get(0);
                if (salesManagerId != null) {
                    customBuilder.salesManager(new FieldDTO.User(salesManagerId));
                }
            }
        }

        // 계약사
        if (baseInfo.getContractor() != null && !baseInfo.getContractor().isEmpty()) {
            customBuilder.contractor(baseInfo.getContractor());
        }

        // 고객사
        if (baseInfo.getClient() != null && !baseInfo.getClient().isEmpty()) {
            customBuilder.contractor(baseInfo.getContractor());
        }

        // 바코드 타입
        FieldInfo barcodeTypeInfo = FieldInfo.ofLabel(FieldInfoCategory.BARCODE_TYPE, String.valueOf(baseInfo.getBarcodeType()));
        if (barcodeTypeInfo != null) {
            customBuilder.barcodeType(new FieldDTO.Field(barcodeTypeInfo.getId()));
        }

        // 팀, 파트
        if (assigneeList != null && !assigneeList.isEmpty()) {
            TB_JIRA_USER_Entity userEntity = TB_JIRA_USER_JpaRepository.findByAccountId(assigneeList.get(0));

            if (userEntity != null) {
                FieldInfo teamInfo = FieldInfo.ofLabel(FieldInfoCategory.TEAM, userEntity.getTeam());
                if (teamInfo != null) {
                    customBuilder.team(teamInfo.getId());
                }

                FieldInfo partInfo = FieldInfo.ofLabel(FieldInfoCategory.PART, userEntity.getPart());
                if (partInfo != null) {
                    customBuilder.part(new FieldDTO.Field(partInfo.getId()));
                }
            }
        }

        // 멀티 OS
        FieldInfo multiOsInfo = FieldInfo.ofLabel(FieldInfoCategory.OS, baseInfo.getSupportType());
        if (multiOsInfo != null) {
            customBuilder.multiOsSupport(Arrays.asList(new FieldDTO.Field(multiOsInfo.getId())));

        }

        // 프린터 지원 범위
        FieldInfo printerSupportRangeInfo = FieldInfo.ofLabel(FieldInfoCategory.PRINTER_SUPPORT_RANGE, baseInfo.getPrinter());
        if (printerSupportRangeInfo != null) {
            customBuilder.printerSupportRange(new FieldDTO.Field(printerSupportRangeInfo.getId()));
        }

        // 기타 - 담당자, 제품 유형, 연동 정보, 담당자 연락처, 점검 방법, url, cabver
        List<String> etcItems = formatEtcField(baseInfo, assignees);
        customBuilder.etc(setDescription(etcItems));

        return customBuilder;
    }

    /*
     *  설명 포맷으로 데이터 변환
     * */
    public FieldDTO.Description setDescription(List<String> textItems) {

        List<FieldDTO.ContentItem> contentItems = new ArrayList<>();

        for (int i = 0; i < textItems.size(); i++) {
            FieldDTO.ContentItem textItem = FieldDTO.ContentItem.builder()
                    .type("text")
                    .text(textItems.get(i))
                    .build();
            contentItems.add(textItem);

            // hardBreak 추가
            if (textItems.size() > 1 && i < textItems.size() - 1) {
                FieldDTO.ContentItem breakItem = FieldDTO.ContentItem.builder()
                        .type("hardBreak")
                        .build();
                contentItems.add(breakItem);
            }
        }

        FieldDTO.Content content = FieldDTO.Content.builder()
                .type("paragraph")
                .content(contentItems)
                .build();

        FieldDTO.Description description = FieldDTO.Description.builder()
                .version(1)
                .type("doc")
                .content(Arrays.asList(content))
                .build();

        return description;
    }

    /*
     *  기타 필드 설정
     * */
    public List<String> formatEtcField(TB_PJT_BASE_Entity baseInfo, String assignees) {

        List<String> etcItems = new ArrayList<>();

        if (assignees != null && !assignees.isEmpty()) {
            etcItems.add("- 담당자: " + assignees);
        }

        String projectStepDescription = getProjectStepDescription(baseInfo.getProjectStep());
        if (projectStepDescription != null) {
            etcItems.add("- 프로젝트 단계: " + projectStepDescription);
        }

        String productTypeDescription = getProductTypeDescription(baseInfo.getProductType());
        if (productTypeDescription != null) {
            etcItems.add("- 제품 유형: " + productTypeDescription);
        }

        appendIfNotEmpty(etcItems, "- 연동 정보: ", baseInfo.getConnectionType());
        appendIfNotEmpty(etcItems, "- 버전: ", baseInfo.getClientType());
        appendIfNotEmpty(etcItems, "- URL: ", baseInfo.getUrl());

        boolean hasClientInfo = false;
        // 고객사 담당자 정보가 있을 경우 타이틀을 먼저 추가
        if (isNotEmpty(baseInfo.getClientName(), "이름") ||
                isNotEmpty(baseInfo.getClientPhoneNumber(), "연락처") ||
                isNotEmpty(baseInfo.getClientEmail(), "메일")) {
            etcItems.add("- 고객사 담당자 정보: ");
            hasClientInfo = true;
        }

        // 고객사 담당자 정보 추가
        hasClientInfo |= appendClientInfo(etcItems, "고객사 담당자: ", baseInfo.getClientName(), "이름");
        hasClientInfo |= appendClientInfo(etcItems, "담당자 연락처: ", baseInfo.getClientPhoneNumber(), "연락처");
        hasClientInfo |= appendClientInfo(etcItems, "담당자 이메일: ", baseInfo.getClientEmail(), "메일");

        appendIfNotEmpty(etcItems, "- 점검 방법: ", baseInfo.getInspectionType());

        if (!etcItems.isEmpty()) {
            etcItems.add(0, "====== 해당 정보는 WSS 백업 데이터 입니다. ======");
        }

        return etcItems;
    }

    private String getProjectStepDescription(int projectStep) {
        switch (projectStep) {
            case 0: return "사전 프로젝트";
            case 1: return "확정 프로젝트";
            case 5: return "무상 유지보수";
            case 7: return "유상 유지보수";
            case 8: return "개발 과제";
            case 9: return "기타";
            case 91: return "유지보수 종료";
            case 92: return "프로젝트 종료(M 필요없음)";
            case 93: return "프로젝트 종료(M 계약)";
            case 94: return "프로젝트 종료(M 미계약)";
            default: return null; // 또는 "알 수 없는 단계";
        }
    }

    private String getProductTypeDescription(int productType) {
        switch (productType) {
            case 10: return "ActiveX";
            case 11: return "Non-ActiveX";
            case 12: return "ZeroClient";
            default: return null; // 또는 "알 수 없는 유형";
        }
    }

    private boolean isNotEmpty(String value, String placeholder) {
        return value != null && !value.isEmpty() && !value.equals(placeholder);
    }

    private boolean appendClientInfo(List<String> items, String prefix, String info, String placeholder) {
        if (info != null && !info.isEmpty() && !info.equals(placeholder)) {
            items.add("  " + prefix + info);
            return true;
        }
        return false;
    }

    private void appendIfNotEmpty(List<String> items, String prefix, String info) {
        if (info != null && !info.isEmpty()) {
            items.add(prefix + info);
        }
    }
    @Override
    public Map<String, String> updateIssueData(TransferIssueDTO transferIssueDTO) throws Exception {

        logger.info("[::TransferIssueImpl::] 프로젝트 코드 -> " + transferIssueDTO.getProjectCode());

        Map<String, String> result = new HashMap<>();

        TB_PJT_BASE_Entity baseInfo = TB_PJT_BASE_JpaRepository.findByProjectCode(transferIssueDTO.getProjectCode());

        TB_JML_Entity jmlEntity = TB_JML_JpaRepository.findByProjectCode(transferIssueDTO.getProjectCode());
        String jiraProjectCode = jmlEntity.getKey();
        String assignees = jmlEntity.getProjectAssignees();
        String jiraIssueKey = null;

        List<String> assigneeList = getSeveralAssigneeId(assignees); // 담당자 리스트

        for (String assignee : assigneeList) {
            logger.info("assignee: " + assignee);
        }

        String projectTitle = "프로젝트 기본 정보";
        String maintenanceTitle = "유지보수 기본 정보";

        CreateIssueDTO<?> createIssueDTO = null;
        if (jmlEntity.getFlag().equals("P")) {
            ProjectInfoDTO.ProjectInfoDTOBuilder<?, ?> projectBuilder = ProjectInfoDTO.builder();

            // 팀, 파트
            if (assigneeList != null && !assigneeList.isEmpty()) {
                TB_JIRA_USER_Entity userEntity = TB_JIRA_USER_JpaRepository.findByAccountId(assigneeList.get(0));

                if (userEntity != null) {
                    FieldInfo teamInfo = FieldInfo.ofLabel(FieldInfoCategory.TEAM, userEntity.getTeam());
                    if (teamInfo != null) {
                        projectBuilder.team(teamInfo.getId());
                        logger.info("[::TransferIssueImpl::] updateIssueData 팀 아이디 -> " + teamInfo.getId());
                    }

                    FieldInfo partInfo = FieldInfo.ofLabel(FieldInfoCategory.PART, userEntity.getPart());
                    if (partInfo != null) {
                        projectBuilder.part(new FieldDTO.Field(partInfo.getId()));
                        logger.info("[::TransferIssueImpl::] updateIssueData 파트 아이디 -> " + partInfo.getId());
                    }
                }
            }

            ProjectInfoDTO projectInfoDTO = projectBuilder.build();
            createIssueDTO = new CreateIssueDTO<>(projectInfoDTO);

            // 기본 정보 이슈 키 조회
            String issueType = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, projectTitle).getId();
            jiraIssueKey = getBaseIssueKey(jiraProjectCode, issueType);
            logger.info("[::TransferIssueImpl::] updateIssueData 이슈타입 -> " + issueType);

        } else {
            MaintenanceInfoDTO.MaintenanceInfoDTOBuilder<?, ?> maintenanceBuilder = MaintenanceInfoDTO.builder();

            // 팀, 파트
            if (assigneeList != null && !assigneeList.isEmpty()) {
                TB_JIRA_USER_Entity userEntity = TB_JIRA_USER_JpaRepository.findByAccountId(assigneeList.get(0));

                if (userEntity != null) {
                    FieldInfo teamInfo = FieldInfo.ofLabel(FieldInfoCategory.TEAM, userEntity.getTeam());
                    if (teamInfo != null) {
                        maintenanceBuilder.team(teamInfo.getId());
                        logger.info("[::TransferIssueImpl::] updateIssueData 팀 아이디 -> " + teamInfo.getId());
                    }

                    FieldInfo partInfo = FieldInfo.ofLabel(FieldInfoCategory.PART, userEntity.getPart());
                    if (partInfo != null) {
                        maintenanceBuilder.part(new FieldDTO.Field(partInfo.getId()));
                        logger.info("[::TransferIssueImpl::] updateIssueData 파트 아이디 -> " + partInfo.getId());
                    }
                }
            }

            // 계약 여부
            FieldInfo contractStatusInfo = FieldInfo.ofLabel(FieldInfoCategory.CONTRACT_STATUS, baseInfo.getContract());
            if (contractStatusInfo != null) {
                maintenanceBuilder.contractStatus(new FieldDTO.Field(contractStatusInfo.getId()));
                logger.info("[::TransferIssueImpl::] updateIssueData 계약 여부 -> " + contractStatusInfo.getId());
            }

            // 점검 방법: 장애 시 지원을 기본으로 설정
            FieldInfo inspectionMethodInfo = FieldInfo.ofLabel(FieldInfoCategory.INSPECTION_METHOD, "장애시 지원");
            maintenanceBuilder.inspectionMethod(new FieldDTO.Field(inspectionMethodInfo.getId()));

            MaintenanceInfoDTO maintenanceInfoDTO = maintenanceBuilder.build();
            createIssueDTO = new CreateIssueDTO<>(maintenanceInfoDTO);

            // 기본 정보 이슈 키 조회
            String issueType = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, maintenanceTitle).getId();
            jiraIssueKey = getBaseIssueKey(jiraProjectCode, issueType);
            logger.info("[::TransferIssueImpl::] updateIssueData 이슈타입 -> " + issueType);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String jsonRequestBody = objectMapper.writeValueAsString(createIssueDTO);
        logger.info("[::TransferIssueImpl::] updateIssueData 최종 DTO -> " + jsonRequestBody);

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());

        // 이슈 업데이트
        if (jiraIssueKey != null) {
            String endpoint = "/rest/api/3/issue/" + jiraIssueKey;
            Optional<Boolean> response = WebClientUtils.executePut(webClient, endpoint, createIssueDTO);
            if (response.isPresent()) {
                if (response.get()) {
                    result.put("jiraIssueKey", jiraIssueKey);
                    result.put("result", "이슈 업데이트 성공");

                    Optional<TB_JML_Entity> entity =  TB_JML_JpaRepository.findById(jiraProjectCode);
                    if(entity.isPresent()){
                        TB_JML_Entity updatedEntity = entity.get();
                        updatedEntity.setUpdateIssueFlag(true);
                        updatedEntity.setUpdateDate(LocalDateTime.now());
                        TB_JML_JpaRepository.save(updatedEntity);
                    }

                    return result;
                }
            }
        }

        result.put("jiraIssueKey", jiraIssueKey);
        result.put("result", "이슈 업데이트 실패");

        return result;
    }

    @Override
    public String getBaseIssueKey(String jiraProjectCode, String issueType) {

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());

        String jql = "project=" + jiraProjectCode + " AND issuetype=" + issueType;
        String fields = "key";
        String endpoint = "/rest/api/3/search?jql=" + jql + "&fields=" + fields;

        return WebClientUtils.get(webClient, endpoint, String.class)
                .map(responseString -> {
                    // JSON 문자열 파싱
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray issues = jsonObject.getJSONArray("issues");
                    if (issues != null && issues.length() > 0) {
                        // 첫 번째 이슈 객체에서 key 값을 추출
                        JSONObject firstIssue = issues.getJSONObject(0);
                        logger.info("[::TransferIssueImpl::] getBaseIssueKey 기본 정보 이슈키 -> " + firstIssue.getString("key"));
                        return firstIssue.getString("key");
                    } else {
                        // 이슈가 없는 경우
                        return null;
                    }
                })
                .block(); // 동기적으로 결과를 기다림
    }

    @Override
    public String getBaseIssueKeyByJiraKey(String jiraKey) {

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());

        TB_JML_Entity projectInfo  = TB_JML_JpaRepository.findByKey(jiraKey);
        String issueType = "";

        if(projectInfo.getFlag().equals("P")){
            issueType = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, "프로젝트 기본 정보").getId();
        }else{
            issueType = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, "유지보수 기본 정보").getId();
        }

        String jql = "project=" + jiraKey + " AND issuetype=" + issueType;
        String fields = "key";
        String endpoint = "/rest/api/3/search?jql=" + jql + "&fields=" + fields;

        return WebClientUtils.get(webClient, endpoint, String.class)
                .map(responseString -> {
                    // JSON 문자열 파싱
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray issues = jsonObject.getJSONArray("issues");
                    if (issues != null && issues.length() > 0) {
                        // 첫 번째 이슈 객체에서 key 값을 추출
                        JSONObject firstIssue = issues.getJSONObject(0);
                        logger.info("[::TransferIssueImpl::] getBaseIssueKey 기본 정보 이슈키 -> " + firstIssue.getString("key"));
                        return firstIssue.getString("key");
                    } else {
                        // 이슈가 없는 경우
                        return null;
                    }
                })
                .block(); // 동기적으로 결과를 기다림
    }


    @Override
    public Specification<TB_JML_Entity> hasDateTimeBeforeIsNull(String field) {
        return (root, query, cb) -> {
            Path<LocalDateTime> updateDate = root.get(field); // 필드 가져오기

            // 현재 날짜에서 하루를 빼서 어제 날짜 생성
            LocalDate yesterday = LocalDate.now().minusDays(1);

            Predicate isNull = cb.isNull(updateDate);
//            Predicate isBefore = cb.lessThanOrEqualTo(updateDate.as(LocalDate.class), yesterday);
//            return cb.or(isNull, isBefore);
            return isNull;
        };
    }

    @Override
    public List<SearchWebLinkDTO> getWebLinkByJiraKey(String jiraKey) throws Exception{
        logger.info("[::TransferIssueImpl::] 웹링크 조회 대상 키 -> " + jiraKey);
        // 기본정보 이슈타입 적용된 이슈아이디 조회
        // 기본 정보 이슈 키 조회
        String projectTitle = "프로젝트 기본 정보";
        String maintenanceTitle = "유지보수 기본 정보";
        String issueType ="";

        TB_JML_Entity projectInfo = TB_JML_JpaRepository.findByKey(jiraKey);
        String flag = projectInfo.getFlag();
        
        if(flag.equals("P")){
            issueType = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, projectTitle).getId();
        } else if (flag.equals("M")) {
            issueType = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, maintenanceTitle).getId();
        }

        String jiraIssueKey = getBaseIssueKey(jiraKey, issueType);
        // 해당 이슈 아이디의 웹 링크 조회

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());

        String endpoint = "/rest/api/3/issue/"+jiraIssueKey+"/remotelink";
        ParameterizedTypeReference<List<SearchWebLinkDTO>> typeRef = new ParameterizedTypeReference<List<SearchWebLinkDTO>>() {};
        /*
        * ParameterizedTypeReference는 Spring Framework에서 제공하는 클래스로, 런타임 시점에 제네릭 타입 정보를 유지할 수 있게 해주는 역할을 함
        * */
        List<SearchWebLinkDTO>  result =  WebClientUtils.get(webClient,endpoint,typeRef).block();

        return result;
    }
    /*
    *  이슈에(이슈키) 프로젝트(프로젝트키) 연결
    * */
    @Override
    public String createWebLink(RequestWeblinkDTO requestWeblinkDTO) throws Exception{
        String issueIdOrKey = requestWeblinkDTO.getIssueIdOrKey();
        String jiraKey = requestWeblinkDTO.getJiraKey();
        String title = requestWeblinkDTO.getTitle();
        logger.info("[::TransferIssueImpl::] 웹링크 연결 정보 -> 연결 이슈: " + issueIdOrKey+" 연결대상 지라 키: "+jiraKey);
        // 웹링크 연결 로직
        String url = "https://markany.atlassian.net/jira/core/projects/" + jiraKey + "/board";

        CreateWebLinkDTO.Icon icon = CreateWebLinkDTO.Icon.builder()
                .url16x16("https://markany.atlassian.net/favicon.ico")
                .build();
        CreateWebLinkDTO.Object object = CreateWebLinkDTO.Object.builder()
                .icon(icon)
                .title(title)
                .url(url)
                .build();

        CreateWebLinkDTO createWebLinkDTO = new CreateWebLinkDTO();
        createWebLinkDTO.setObject(object);

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint = "/rest/api/3/issue/"+issueIdOrKey+"/remotelink";
        String result =  WebClientUtils.post(webClient,endpoint,createWebLinkDTO,String.class).block();

        return result;
    }
    /*
    *  두개의 지라키로 양방향으로 웹링크 거는 로직 + 디비 업데이트
    * */
    @Override
    public Boolean createWebLinkBothSides(String mainJiraKey, String subJiraKey) throws Exception {
        logger.info("[::TransferIssueImpl::] 웹링크 양방향 생성 -> " + mainJiraKey + "  " + subJiraKey);
        // mainJiraKey에 subJiraKey 걸기
        TB_JML_Entity mainInfo = TB_JML_JpaRepository.findByKey(mainJiraKey);
        TB_JML_Entity subInfo = TB_JML_JpaRepository.findByKey(subJiraKey);

        if (mainInfo != null && subInfo != null) {
            String mainIssueKeyOrId = getBaseIssueKeyByJiraKey(mainJiraKey);
            String subTitle = subInfo.getJiraProjectName();

            RequestWeblinkDTO main = new RequestWeblinkDTO();
            main.setIssueIdOrKey(mainIssueKeyOrId);
            main.setJiraKey(subJiraKey);
            main.setTitle(subTitle);
            String mainResult = createWebLink(main);

            // subJiraKey에 mainJiraKey 걸기
            String subIssueKeyOrId = getBaseIssueKeyByJiraKey(subJiraKey);
            String mainTitle = mainInfo.getJiraProjectName();

            RequestWeblinkDTO sub = new RequestWeblinkDTO();
            sub.setIssueIdOrKey(subIssueKeyOrId);
            sub.setJiraKey(mainJiraKey);
            sub.setTitle(mainTitle);
            String subResult = createWebLink(sub);

            if (mainResult != null && subResult != null) {
                TB_JLL_Entity entity = TB_JLL_JpaRepository.findByParentKeyAndChildKey(mainJiraKey, subJiraKey);
                entity.setLinkCheckFlag(true);
                TB_JLL_Entity savedEntity = TB_JLL_JpaRepository.save(entity);
                if (savedEntity != null) {
                    return true;
                }
            }
        }

        return false;
    }



    /*
    *  댓글 삭제
    * */
    @Override
    public void deleteComment(String issueIdOrKey,String id) throws Exception{

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint = "/rest/api/3/issue/"+issueIdOrKey+"/comment"+id;

        WebClientUtils.delete(webClient,endpoint,Void.class);

    }
    /*
    *  댓글 조회
    * */
    @Override
    public CommentDTO getComment(String issueIdOrKey) throws Exception{

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint = "/rest/api/3/issue/"+issueIdOrKey+"/comment";

        CommentDTO result = WebClientUtils.get(webClient,endpoint,CommentDTO.class).block();

        return result ;
    }

}
