package com.transfer.issue.service;


import com.account.dto.AdminInfoDTO;
import com.account.entity.TB_JIRA_USER_Entity;
import com.account.service.Account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transfer.issue.model.FieldInfo;
import com.transfer.issue.model.FieldInfoCategory;
import com.transfer.issue.model.dao.PJ_PG_SUB_JpaRepository;
import com.transfer.issue.model.dto.*;
import com.transfer.issue.model.entity.PJ_PG_SUB_Entity;
import com.transfer.project.model.dao.TB_PJT_BASE_JpaRepository;
import com.transfer.project.model.entity.TB_JML_Entity;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
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

        List<PJ_PG_SUB_Entity> issueList = PJ_PG_SUB_JpaRepository.findAllByProjectCodeOrderByCreationDateAsc(projectCode);

        if(issueList.isEmpty()){
            createBaseInfoIssue(issueList, project);
            result.put(projectCode, "이슈 생성 성공");
        }else{
            if(createBaseInfoIssue(issueList, project)){
                ResponseIssueDTO issue  = createWssHistoryIssue(issueList, project);
                if(issue != null){
                    System.out.println("상태변경 대상 키"+issue.getKey());
                    changeIssueStatus(issue.getKey());
                    CheckIssueMigrateFlag(projectCode);
                    result.put(projectCode, "이슈 생성 성공");
                }else{
                    result.put(projectCode,"이슈 생성 실패");
                }
            }else{
                result.put(projectCode,"이슈 생성 실패");
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
            creationDate = String.valueOf(issueList.get(0).getCreationDate());
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
            RelatedProject(wssProjectCode,responseIssueDTO.getKey());
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

        if (userNames != null && !userNames.trim().isEmpty()) {

            if (validateAssigneeFormat(userNames)) {

                String[] namesArray = userNames.trim().split("\\s*,\\s*");

                List<String> namesArrayList = Arrays.asList(namesArray);
                namesArrayList.subList(0, Math.min(namesArrayList.size(), 2));

                List<String> userIdList = new ArrayList<>();
                for (String name : namesArrayList) {

                    Optional<TB_JIRA_USER_Entity> userEntity = TB_JIRA_USER_JpaRepository.findByDisplayNameContaining(name)
                            .stream()
                            .findFirst();

                    userEntity.ifPresent(entity -> {
                        // userEntity가 존재하는 경우
                        String userId = userEntity.get().getAccountId();
                        if (userId != null) {
                            userIdList.add(userId);
                        }
                    });
                }
                return userIdList;
            }
        } else {
            // 담당자 미지정된 프로젝트 (전자문서사업부 아이디)
            return null;
        }
        return null;
    }

    /*
     *  이슈 담당자 이름으로 지라서버 아이디 디비 검색
     * */
    public String getOneAssigneeId(String userName) throws Exception {
        logger.info("[::TransferIssueImpl::] getOneAssigneeId");

        List<TB_JIRA_USER_Entity> user = TB_JIRA_USER_JpaRepository.findByDisplayNameContaining(userName);
        if (!user.isEmpty()) {
            String userId = user.get(0).getAccountId();
            return userId;
        } else {
            return null; // 담당자가 관리 목록에 없으면 전자문서 사업부 기본아이디로 삽입
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
        CreateIssueDTO<?> createIssueDTO = null;

        String jiraProjectId = migratedProject.getId();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedToday = today.format(formatter);

        FieldDTO fieldDTO = new FieldDTO();
        // 담당자
        /*FieldDTO.User user = FieldDTO.User.builder()
                .accountId(wssAssignee).build();
        fieldDTO.setAssignee(user);*/

        // 프로젝트 아이디
        FieldDTO.Project project = FieldDTO.Project.builder()
                .id(jiraProjectId)
                .build();
        fieldDTO.setProject(project);


        // wss 이슈 제목
        String summary = "["+formattedToday+"] WSS HISTORY";
        fieldDTO.setSummary(summary);


        // wss 작성이슈 내용
        String wssContents = "";
        for (PJ_PG_SUB_Entity issue : issueList){

            Date  date = issue.getCreationDate();
            String title = "작성일: " + date +"     작성자: "+issue.getWriter();
            String content = issue.getIssueContent().replace("<br>", "\n").replace("&nbsp;", " ");;
            String divider = "====================================================================";
            String contentItem = title+"\n"+content+"\n\n"+divider+"\n\n";

            wssContents += contentItem;
        }

        FieldDTO.ContentItem contentItem = FieldDTO.ContentItem.builder()
                .type("text")
                .text(wssContents)
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

        FieldDTO.Field field =  FieldDTO.Field.builder()
                .id("10002")
                .build();
        fieldDTO.setIssuetype(field);

        createIssueDTO = new CreateIssueDTO<>(fieldDTO);

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint ="/rest/api/3/issue";

        Flux<ResponseIssueDTO> response = WebClientUtils.postByFlux(webClient, endpoint, createIssueDTO, ResponseIssueDTO.class);
        return response.blockFirst();
    }

    public boolean CheckIssueMigrateFlag(String projectCode) throws Exception{

        logger.info("[::TransferIssueImpl::] CheckMigrateFlag");
        TB_PJT_BASE_Entity entity =  TB_PJT_BASE_JpaRepository.findById(projectCode).orElseThrow(() -> new NoSuchElementException("프로젝트 코드 조회에 실패하였습니다.: " + projectCode));
        entity.setIssueMigrateFlag(true);
        return true;
    }

    public void RelatedProject(String projectCode, String issueIdOrKey) throws JsonProcessingException {
        // 프로젝트 조회해서 relatedProject 가 projectCode인 프로젝트 리스트 구하기
        List<TB_PJT_BASE_Entity> relatedProjectList = TB_PJT_BASE_JpaRepository.findByRelatedProject(projectCode);

        if (relatedProjectList == null || relatedProjectList.isEmpty()) {
            System.out.println("해당 프로젝트는 연관된 프로젝트가 없습니다.");
            return;
        }

        List<String> relatedLinkList = relatedProjectList.stream()
                .map(relatedProject -> {
                    String relatedProjectCode = relatedProject.getProjectCode();
                    TB_JML_Entity migratedProject = TB_JML_JpaRepository.findByProjectCode(relatedProjectCode);
                    if (migratedProject == null) {
                        return "[연관된 프로젝트(지라 프로젝트 이관 전)]" + relatedProjectCode;
                    } else {
                        return migratedProject.getKey() != null ?
                                "\n[연관된 프로젝트(지라 프로젝트 이관 완료)]\nhttps://markany.atlassian.net/jira/core/projects/" + migratedProject.getKey() + "/board\n" : "\n[연관된 프로젝트(지라 프로젝트 이관 전)]" + relatedProjectCode +"\n";
                    }
                })
                .collect(Collectors.toList());

        String result = "";
        for (String content : relatedLinkList){
            result += content;
        }
        System.out.println(result);

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
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(addCommentDTO);
        System.out.println(json);

        String endpoint = "/rest/api/3/issue/"+issueIdOrKey+"/comment";
        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        WebClientUtils.post(webClient,endpoint,addCommentDTO,String.class).block();
    }

    // 프로젝트와 유지보수 공통 필드 설정
    public <B extends CustomFieldDTO.CustomFieldDTOBuilder<?, ?>> B setCommonFields(B customBuilder, TB_PJT_BASE_Entity baseInfo, String jiraProjectKey, String baseDescription, String assignees) throws Exception {

        // 프로젝트
        customBuilder.project(new FieldDTO.Project(jiraProjectKey, null));

        // 설명
        customBuilder.description(setDescription(baseDescription));

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

        // 바코드 타입
        FieldInfo barcodeTypeInfo = FieldInfo.ofLabel(FieldInfoCategory.BARCODE_TYPE, String.valueOf(baseInfo.getBarcodeType()));
        if (barcodeTypeInfo != null) {
            customBuilder.barcodeType(new FieldDTO.Field(barcodeTypeInfo.getId()));
        }

        // 계약사
        if (baseInfo.getContractor() != null && !baseInfo.getContractor().isEmpty()) {
            customBuilder.contractor(baseInfo.getContractor());
        }

        // 고객사
        if (baseInfo.getClient() != null && !baseInfo.getClient().isEmpty()) {
            customBuilder.contractor(baseInfo.getContractor());
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

        // 기타 - 제품 유형, 연동 정보, 담당자 연락처, 점검 방법
        String etc = "";
        if (baseInfo.getProductType() != 0) {
            etc += "- 제품 유형: ";
            if (baseInfo.getProductType() == 10) {
                etc += "ActiveX\n";
            } else if (baseInfo.getProductType() == 11) {
                etc += "Non-ActiveX\n";
            } else if (baseInfo.getProductType() == 12) {
                etc += "ZeroClient\n";
            }
        }
        if (baseInfo.getConnectionType() != null && !baseInfo.getConnectionType().isEmpty()) {
            etc += "- 연동 정보: " + baseInfo.getConnectionType() + "\n";
        }

        boolean hasClientInfo = false;  // 고객사 담당자 정보가 있는지 확인하기 위한 플래그
        if (baseInfo.getClientName() != null && !baseInfo.getClientName().isEmpty() && !baseInfo.getClientName().equals("이름")) {
            if (!hasClientInfo) {
                etc += "- 고객사 담당자 정보: \n";
                hasClientInfo = true;
            }
            etc += "  고객사 담당자: " + baseInfo.getClientName() + "\n";
        }
        if (baseInfo.getClientPhoneNumber() != null && !baseInfo.getClientPhoneNumber().isEmpty() && !baseInfo.getClientPhoneNumber().equals("연락처")) {
            if (!hasClientInfo) {
                etc += "- 고객사 담당자 정보: \n";
                hasClientInfo = true;
            }
            etc += "  담당자 연락처: " + baseInfo.getClientPhoneNumber() + "\n";
        }
        if (baseInfo.getClientEmail() != null && !baseInfo.getClientEmail().isEmpty() && !baseInfo.getClientEmail().equals("메일")) {
            if (!hasClientInfo) {
                etc += "- 고객사 담당자 정보: \n";
                hasClientInfo = true;
            }
            etc += "  담당자 이메일: " + baseInfo.getClientEmail() + "\n";
        }

        if (baseInfo.getInspectionType() != null && !baseInfo.getInspectionType().isEmpty()) {
            etc += "- 점검 방법: " + baseInfo.getInspectionType() + "\n";
        }

        customBuilder.etc(setDescription(etc));

        return customBuilder;
    }

    public FieldDTO.Description setDescription(String text) {
        FieldDTO.ContentItem contentItem = FieldDTO.ContentItem.builder()
                .type("text")
                .text(text)
                .build();

        FieldDTO.Content content = FieldDTO.Content.builder()
                .content(Arrays.asList(contentItem))
                .type("paragraph")
                .build();

        FieldDTO.Description description = FieldDTO.Description.builder()
                .version(1)
                .type("doc")
                .content(Arrays.asList(content))
                .build();

        return description;
    }

    /*
     *  벌크로 이슈 생성 12/19 회의 결과 미사용으로 전환
     * */
    /*public boolean createBulkIssue(List<PJ_PG_SUB_Entity> issueList , String jiraProjectId) throws Exception {
        logger.info("[::TransferIssueImpl::] createBulkIssue");
        List<PJ_PG_SUB_Entity> nomalIssueList = issueList.subList(1, issueList.size());

        CreateBulkIssueDTO bulkIssueDTO = new CreateBulkIssueDTO();

        List<CreateBulkIssueFieldsDTO> issueUpdates = new ArrayList<>();

        for(PJ_PG_SUB_Entity issueData : nomalIssueList){

            String wssAssignee = getOneAssigneeId(issueData.getWriter());

            String wssContent  = issueData.getIssueContent();
            Date wssWriteDate  = issueData.getCreationDate();

            String defaultIssueContent = "\n[" + wssWriteDate  + "]\n본 이슈는 WSS에서 이관한 이슈입니다.\n―――――――――――――――――――――――――――――――\n"; // 이슈 생성 시 기본 문구
            String replacedIssueContent = wssContent.replace("<br>", "\n").replace("&nbsp;", " "); // 이슈 내용 전처리
            String basicIssueContent = defaultIssueContent + replacedIssueContent; // 이슈 내용

            FieldDTO fieldDTO = new FieldDTO();
            // 담당자
            FieldDTO.User user = FieldDTO.User.builder()
                            .accountId(wssAssignee).build();
            fieldDTO.setAssignee(user);

            // 프로젝트 아이디
            FieldDTO.Project project = FieldDTO.Project.builder()
                    .id(jiraProjectId)
                    .build();
            fieldDTO.setProject(project);


            // wss 이슈 제목
            String summary = "["+wssWriteDate+"] WSS 작성이슈";
            fieldDTO.setSummary(summary);

            // wss 이슈
            FieldDTO.ContentItem contentItem = FieldDTO.ContentItem.builder()
                    .type("text")
                    .text(basicIssueContent)
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

            FieldDTO.Field field =  FieldDTO.Field.builder()
                            .id("10002")
                            .build();
            fieldDTO.setIssuetype(field);

            CreateBulkIssueFieldsDTO fields = new CreateBulkIssueFieldsDTO();
            fields.setFields(fieldDTO);

            issueUpdates.add(fields);

        }

        bulkIssueDTO.setIssueUpdates(issueUpdates);

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint ="/rest/api/3/issue/bulk";

        Flux<ResponseBulkIssueDTO> response = WebClientUtils.postByFlux(webClient,endpoint,bulkIssueDTO,ResponseBulkIssueDTO.class);

        response.subscribe(
                resp -> System.out.println(resp),  // onNext
                error -> System.out.println("Error: " + error.getMessage()),  // onError
                () -> System.out.println("Completed")  // onComplete
        );

        Mono<List<ResponseBulkIssueDTO>> mono = response.collectList();
        //Flux는 여러 개의 데이터를 스트림으로 처리하는데 사용되는 반면, Mono는 하나의 데이터를 비동기적으로 처리하는데 사용됩니다. Flux의 collectList() 메소드를 사용하면 Flux를 Mono로 변환
        List<ResponseBulkIssueDTO> responseList = mono.block();
        // Mono의 block() 메소드를 사용하면 비동기 작업이 완료될 때까지 현재 스레드를 대기 상태로 만듬
        if (responseList != null && responseList.stream().allMatch(resp -> resp.getErrors() == null)) {
            return true;
        } else {
            return false;
        }
    }*/


}
