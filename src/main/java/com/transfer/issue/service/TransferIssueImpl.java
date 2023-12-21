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
import com.transfer.issue.model.dto.bulk.CreateBulkIssueDTO;
import com.transfer.issue.model.dto.bulk.CreateBulkIssueFieldsDTO;
import com.transfer.issue.model.dto.bulk.ResponseBulkIssueDTO;
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
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    public Boolean createBaseInfoIssue(List<PJ_PG_SUB_Entity> issueList , TB_JML_Entity migratedProject) throws Exception {
        logger.info("[::TransferIssueImpl::] 기본 정보 이슈 생성 시작");

        // 1. firstIssue에서 WSS 키 가져오기
        // 2. BASE 테이블에서 필요한 필드 값 가져오기
        // 3. jiraProjectKey를 프로젝트 키로 하여, 필요한 값 할당해서 이슈 생성

        String jiraProjectKey = migratedProject.getKey();
        String projectAssignees = migratedProject.getProjectAssignees();
        String wssProjectCode = migratedProject.getProjectCode();
        String creationDate ;

        if(issueList.isEmpty()){
            creationDate = null;
        }else{
            creationDate = String.valueOf(issueList.get(0).getCreationDate());
        }

        TB_PJT_BASE_Entity basicInfo = TB_PJT_BASE_JpaRepository.findByProjectCode(wssProjectCode);

        // WSS에서 이관할 데이터
        WssIssueDTO wssIssue = new WssIssueDTO();
        wssIssue.setProjectCode(wssProjectCode);
        wssIssue.setCreationDate(creationDate);

        List<String> assignees = getSeveralAssigneeId(projectAssignees); // 프로젝트 담당자 리스트

        // 담당자 및 부 담당자 설정
        FieldDTO.User assignee = null;
        FieldDTO.User subAssignee = null;
        if (assignees != null) {
            if (assignees.size() >= 1) {
                assignee = new FieldDTO.User(assignees.get(0));
            }
            if (assignees.size() == 2) {
                subAssignee = new FieldDTO.User(assignees.get(1));
            }
        }

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedToday = today.format(formatter);

        // 설명 설정
        String defaultIssueContent = "["
                + formattedToday
                + "]\n본 이슈는 WSS에서 이관한 이슈입니다.";

        String basicIssueContent = defaultIssueContent;
        // WSS에서 이관할 데이터를 프로젝트 타입에 맞는 DTO에 set
        System.out.println("[::TransferIssueImpl::] wssProjectCode -> " + wssProjectCode);

        // 프로젝트, 유지보수 공통
        FieldDTO.Project project = new FieldDTO.Project(jiraProjectKey, null);

        FieldDTO.ContentItem contentItem = FieldDTO.ContentItem.builder()
                .type("text")
                .text(basicIssueContent)
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



        // 이슈 생성
        CreateIssueDTO<?> createIssueDTO = null;
        ProjectInfoDTO projectInfoDTO = null;
        MaintenanceInfoDTO maintenanceInfoDTO = null;

        String projectBasicInfo = "프로젝트 기본 정보";
        String maintenanceBasicInfo = "유지보수 기본 정보";

        // 프로젝트인지 유지보수인지 판별
        if (basicInfo.getProjectFlag().equals("P")) {

            // 필수 데이터
            ProjectInfoDTO.ProjectInfoDTOBuilder projectInfoDTOBuilder = ProjectInfoDTO.builder()
                    .project(project) // 프로젝트
                    .summary(projectBasicInfo) // 제목
                    .description(description); // 설명

            // 담당자
            if (assignee != null) {
                projectInfoDTOBuilder.assignee(assignee);
            }

            // 부 담당자
            if (subAssignee != null) {
                projectInfoDTOBuilder.subAssignee(subAssignee);
            }

            // 이슈타입
            FieldInfo issueTypeFieldInfo = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, projectBasicInfo);
            if (issueTypeFieldInfo != null) {
                projectInfoDTOBuilder.issuetype(new FieldDTO.Field(issueTypeFieldInfo.getId()));
            }

            // 영업대표
            if (basicInfo.getSalesManager() != null) {
                if (getSeveralAssigneeId(basicInfo.getSalesManager()) != null && getSeveralAssigneeId(basicInfo.getSalesManager()).size() >= 1) {
                    String salesManagerId = getOneAssigneeId(getSeveralAssigneeId(basicInfo.getSalesManager()).get(0));
                    if (salesManagerId != null) {
                        projectInfoDTO.setSalesManager(new FieldDTO.User(salesManagerId));
                    }
                }
            }

            // 바코드 타입
            FieldInfo barcodeTypeInfo = FieldInfo.ofLabel(FieldInfoCategory.BARCODE_TYPE, String.valueOf(basicInfo.getBarcodeType()));
            if (barcodeTypeInfo != null) {
                projectInfoDTOBuilder.barcodeType(new FieldDTO.Field(barcodeTypeInfo.getId()));
            }

            // 팀, 파트
            if (assignees != null && assignees.size() >= 1) {
                TB_JIRA_USER_Entity userEntity = TB_JIRA_USER_JpaRepository.findByAccountId(assignees.get(0));

                if (userEntity != null) {
                    FieldInfo teamInfo = FieldInfo.ofLabel(FieldInfoCategory.TEAM, userEntity.getTeam());
                    if (teamInfo != null) {
                        projectInfoDTOBuilder.team(teamInfo.getId());
                    }

                    FieldInfo partInfo = FieldInfo.ofLabel(FieldInfoCategory.PART, userEntity.getPart());
                    if (partInfo != null) {
                        projectInfoDTOBuilder.part(new FieldDTO.Field(partInfo.getId()));
                    }
                }
            }

            // 멀티 OS
            FieldInfo multiOsInfo = FieldInfo.ofLabel(FieldInfoCategory.OS, basicInfo.getSupportType());
            if (multiOsInfo != null) {
                projectInfoDTOBuilder.multiOsSupport(Arrays.asList(new FieldDTO.Field(multiOsInfo.getId())));

            }

            // 프린터 지원 범위
            FieldInfo printerSupportRangeInfo = FieldInfo.ofLabel(FieldInfoCategory.PRINTER_SUPPORT_RANGE, basicInfo.getPrinter());
            if (printerSupportRangeInfo != null) {
                projectInfoDTOBuilder.printerSupportRange(new FieldDTO.Field(printerSupportRangeInfo.getId()));
            }

            // 프로젝트 진행 단계
            String projectProgressStepId = FieldInfo.getIdByCategoryAndLabel(
                    FieldInfoCategory.PROJECT_PROGRESS_STEP,
                    String.valueOf(basicInfo.getProjectStep()));
            if (projectProgressStepId != null) {
                projectInfoDTOBuilder.projectProgressStep(new FieldDTO.Field(projectProgressStepId));
            }

            // 프로젝트명
            if (basicInfo.getProjectName() != null) {
                projectInfoDTOBuilder.projectName(basicInfo.getProjectName());
            }

            // 프로젝트 코드
            if (basicInfo.getProjectCode() != null) {
                projectInfoDTOBuilder.projectCode(basicInfo.getProjectCode());
            }

            // 프로젝트 배정일
            if (wssIssue.getCreationDate() != null) {
                projectInfoDTOBuilder.projectAssignmentDate(wssIssue.getCreationDate());
            }

            projectInfoDTO = projectInfoDTOBuilder.build();
            createIssueDTO = new CreateIssueDTO<>(projectInfoDTO);
        } else {
            maintenanceInfoDTO = new MaintenanceInfoDTO();

            // =============================이슈 기본 정보=============================
            // 이슈 제목
            MaintenanceInfoDTO.MaintenanceInfoDTOBuilder maintenanceInfoDTOBuilder = MaintenanceInfoDTO.builder()
                    .project(project) // 프로젝트
                    .summary(maintenanceBasicInfo) // 제목
                    .description(description); // 설명

            // 유지보수 명
            if (basicInfo.getProjectName() != null) {
                maintenanceInfoDTOBuilder.maintenanceName(basicInfo.getProjectName());

            }
            // 이슈타입
            FieldInfo issueTypeFieldInfo = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, maintenanceBasicInfo);
            if (issueTypeFieldInfo != null) {
                maintenanceInfoDTOBuilder.issuetype(new FieldDTO.Field(issueTypeFieldInfo.getId()));
            }
            //========================================================================


            // =============================필드 기본 정보=============================
            // 유지보수 코드
            if (basicInfo.getProjectCode() != null) {
                maintenanceInfoDTOBuilder.maintenanceCode(basicInfo.getProjectCode());
            }

            // 담당자
            if (assignee != null) {
                maintenanceInfoDTOBuilder.assignee(assignee);
            }

            // 부 담당자
            if (subAssignee != null) {
                maintenanceInfoDTOBuilder.subAssignee(subAssignee);
            }

            // 영업대표
            if (basicInfo.getSalesManager() != null) {
                if (getSeveralAssigneeId(basicInfo.getSalesManager()) != null && getSeveralAssigneeId(basicInfo.getSalesManager()).size() >= 1) {
                    String salesManagerId = getOneAssigneeId(getSeveralAssigneeId(basicInfo.getSalesManager()).get(0));
                    if (salesManagerId != null) {
                        maintenanceInfoDTO.setSalesManager(new FieldDTO.User(salesManagerId));
                    }
                }
            }

            // 바코드 타입
            FieldInfo barcodeTypeInfo = FieldInfo.ofLabel(FieldInfoCategory.BARCODE_TYPE, String.valueOf(basicInfo.getBarcodeType()));
            if (barcodeTypeInfo != null) {
                maintenanceInfoDTOBuilder.barcodeType(new FieldDTO.Field(barcodeTypeInfo.getId()));
            }

            // 팀, 파트
            if (assignees != null && assignees.size() >= 1) {
                TB_JIRA_USER_Entity userEntity = TB_JIRA_USER_JpaRepository.findByAccountId(assignees.get(0));

                if (userEntity != null) {
                    FieldInfo teamInfo = FieldInfo.ofLabel(FieldInfoCategory.TEAM, userEntity.getTeam());
                    if (teamInfo != null) {
                        maintenanceInfoDTOBuilder.team(teamInfo.getId());
                    }

                    FieldInfo partInfo = FieldInfo.ofLabel(FieldInfoCategory.PART, userEntity.getPart());
                    if (partInfo != null) {
                        maintenanceInfoDTOBuilder.part(new FieldDTO.Field(partInfo.getId()));
                    }
                }
            }

            // 멀티 OS
            FieldInfo multiOsInfo = FieldInfo.ofLabel(FieldInfoCategory.OS, basicInfo.getSupportType());
            if (multiOsInfo != null) {
                maintenanceInfoDTOBuilder.multiOsSupport(Arrays.asList(new FieldDTO.Field(multiOsInfo.getId())));

            }

            // 프린터 지원 범위
            FieldInfo printerSupportRangeInfo = FieldInfo.ofLabel(FieldInfoCategory.PRINTER_SUPPORT_RANGE, basicInfo.getPrinter());
            if (printerSupportRangeInfo != null) {
                maintenanceInfoDTOBuilder.printerSupportRange(new FieldDTO.Field(printerSupportRangeInfo.getId()));
            }


            maintenanceInfoDTO = maintenanceInfoDTOBuilder.build();
            createIssueDTO = new CreateIssueDTO<>(maintenanceInfoDTO);
            
        }

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());

        String endpoint = "/rest/api/3/issue";
        ResponseIssueDTO responseIssueDTO = null;
        try {
            responseIssueDTO = WebClientUtils.post(webClient, endpoint, createIssueDTO, ResponseIssueDTO.class).block();
        } catch (Exception e) {
            if (e instanceof WebClientResponseException) {
                WebClientResponseException wcException = (WebClientResponseException) e;
                HttpStatus status = wcException.getStatusCode();
                String body = wcException.getResponseBodyAsString();

                System.out.println(status + " : " + body);
            }
        }

        if(responseIssueDTO.getKey() != null){
            changeIssueStatus(responseIssueDTO.getKey());
            return true;
        }else{
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
            String[] namesArray = userNames.trim().split("\\s*,\\s*");

            List<String> namesArrayList = Arrays.asList(namesArray);

            List<String> userIdList = new ArrayList<>();

            for (String name : namesArrayList) {
                List<TB_JIRA_USER_Entity> users = TB_JIRA_USER_JpaRepository.findByDisplayNameContaining(name);

                if (!users.isEmpty()) { //조회 대상정보가 디비에 없을 때
                    String userId = users.get(0).getAccountId();
                    userIdList.add(userId);
                }else{
                    return null;
                }
            }
            // 앞에 2개의 데이터만 추출하여 반환
            return userIdList.subList(0, Math.min(userIdList.size(), 2));
        } else {
            // 담당자 미지정된 프로젝트 (전자문서사업부 아이디)
            return null;
        }
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
            String title = "작성일 :" + date +" 작성자 :"+issue.getWriter();
            String content = issue.getIssueContent().replace("<br>", "\n").replace("&nbsp;", " ");;
            String divider = "===========================================================================";
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
