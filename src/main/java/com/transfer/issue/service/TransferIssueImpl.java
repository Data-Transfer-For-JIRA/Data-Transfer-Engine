package com.transfer.issue.service;


import com.account.dto.AdminInfoDTO;
import com.account.entity.TB_JIRA_USER_Entity;
import com.account.service.Account;
import com.transfer.issue.model.FieldInfo;
import com.transfer.issue.model.FieldInfoCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transfer.issue.model.FieldInfo;
import com.transfer.issue.model.dao.PJ_PG_SUB_JpaRepository;
import com.transfer.issue.model.dto.*;
import com.transfer.issue.model.entity.PJ_PG_SUB_Entity;
import com.transfer.project.model.dao.TB_PJT_BASE_JpaRepository;
import com.transfer.project.model.entity.TB_JML_Entity;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;
import com.utils.ProjectConfig;
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

import java.awt.desktop.SystemSleepEvent;
import java.util.*;

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
        logger.info("이슈 생성 시작");
        Map<String, String> result = new HashMap<>();

        // 생성할 프로젝트 조회
        TB_JML_Entity project = checkProjectCreated(transferIssueDTO.getProjectCode());
        // 지라 프로젝트 키
        String jiraProjectKey  = project.getKey();
        // 지라 프로젝트 아이디
        String jiraProjectId = project.getId();
        // 프로젝트 담당자 조회
        String projectAssignees = project.getProjectAssignees();

        if(project == null){
            logger.info("생성된 프로젝트가 아닙니다.");
            result.put("해당 프로젝트는 지라에없습니다.",transferIssueDTO.getProjectCode());
        }else{
            // WSS 데이터 조회
            logger.info("이슈생성을 시작합니다.");
            // 이슈 시간 기준 오름 차순 조회
            List<PJ_PG_SUB_Entity> issueList= PJ_PG_SUB_JpaRepository.findAllByProjectCodeOrderByCreationDateAsc(transferIssueDTO.getProjectCode());

            if(createFirstIssue(issueList,jiraProjectKey,projectAssignees)){
                logger.info("최초 이슈 생성 성공");
                createBulkIssue(issueList,jiraProjectId);
            }else{
                result.put("이슈 생성 실패",transferIssueDTO.getProjectCode());
            }
        }
        return result;
    }
    public TB_JML_Entity checkProjectCreated(String projectCode){
        logger.info("자리에 생성된 프로젝트 조회");
       return TB_JML_JpaRepository.findByProjectCode(projectCode);
    }


    public Boolean createFirstIssue(List<PJ_PG_SUB_Entity> issueList, String jiraProjectKey, String projectAssignees) throws Exception {
        logger.info("최초 이슈 생성 시도");

        // firstIssue null 체크 필요
        PJ_PG_SUB_Entity firstIssue = issueList.get(0); // 이슈 최초 컨텐츠

        String issueContent = firstIssue.getIssueContent(); // 이슈 내용
        Date creationDate   = firstIssue.getCreationDate(); // 이슈 생성일
        List<String> assignees = getSeveralAssigneeId(projectAssignees); // 프로젝트 담당자 리스트

        // 담당자 지정
        FieldDTO.User assignee = new FieldDTO.User(assignees.get(0));
        FieldDTO.User sub_assignee = null;
        if (assignees.size() == 2) {
            sub_assignee = new FieldDTO.User(assignees.get(1));
        }

//        for (String assignee : assignees) {
//            System.out.println("[::TransferIssueImpl::] assignee -> " + assignee);
//        }

        String defaultIssueContent = "\n[" + creationDate  + "]\n본 이슈는 WSS에서 이관한 이슈입니다.\n―――――――――――――――――――――――――――――――\n"; // 이슈 생성 시 기본 문구
        String replacedIssueContent = issueContent.replace("<br>", "\n").replace("&nbsp;", "    "); // 이슈 내용 전처리
        String basicIssueContent = defaultIssueContent + replacedIssueContent; // 이슈 내용

        // 1. firstIssue에서 WSS 키 가져오기
        // 2. BASE 테이블에서 필요한 필드 값 가져오기
        // 3. jiraProjectKey를 프로젝트 키로 하여, 필요한 값 할당해서 이슈 생성
        String wssProjectCode = firstIssue.getProjectCode();
        System.out.println("[::TransferIssueImpl::] wssProjectCode -> " + wssProjectCode);
        TB_PJT_BASE_Entity basicInfo = TB_PJT_BASE_JpaRepository.findByProjectCode(wssProjectCode);
        System.out.println("[::TransferIssueImpl::] basicInfo project code -> " + basicInfo.getProjectCode());

        CreateIssueDTO<?> createIssueDTO = null;
        ProjectInfoDTO projectInfoDTO = null;
        MaintenanceInfoDTO maintenanceInfoDTO = null;

        String projectBasicInfo = "프로젝트 기본 정보";
        String maintenanceBasicInfo = "유지보수 기본 정보";

        // 프로젝트인지 유지보수인지 판별
        if (basicInfo.getProjectFlag().equals("P")) {

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

            FieldDTO.User salesManager = null;
            String salesManagerId = getOneAssigneeId(getSeveralAssigneeId(basicInfo.getSalesManager()).get(0));
            if (salesManagerId != null) {
                salesManager = new FieldDTO.User(salesManagerId);
            }

            FieldDTO.Field barcodeType = null;
            String barcodeId = FieldInfo.ofLabel(FieldInfoCategory.BARCODE_TYPE, String.valueOf(basicInfo.getBarcodeType())).getId();
            if (barcodeId != null) {
                barcodeType = new FieldDTO.Field(barcodeId);
            }

            String team = null;
            FieldDTO.Field part = null;
            TB_JIRA_USER_Entity userEntity = TB_JIRA_USER_JpaRepository.findByAccountId(assignees.get(0));
            String teamId = FieldInfo.ofLabel(FieldInfoCategory.TEAM, userEntity.getTeam()).getId();
            String partId = FieldInfo.ofLabel(FieldInfoCategory.PART, userEntity.getPart()).getId();
            System.out.println("[::TransferIssueImpl::] userEntity name -> " + userEntity.getDisplayName());
            System.out.println("[::TransferIssueImpl::] userEntity team -> " + userEntity.getTeam());
            System.out.println("[::TransferIssueImpl::] userEntity part -> " + userEntity.getPart());


            if (teamId != null) {
                team = teamId;
            }

            if (partId != null) {
                part = new FieldDTO.Field(partId);
            }

            FieldDTO.Field multiOS = null;
            FieldInfo multiOSFieldInfo = FieldInfo.ofLabel(FieldInfoCategory.OS, basicInfo.getSupportType());
            if (multiOSFieldInfo != null) {
                String multiOSId = multiOSFieldInfo.getId();
                if (multiOSId != null) {
                    multiOS = new FieldDTO.Field(multiOSId);
                }
            }

            FieldDTO.Field printerSupportRange = null;
            FieldInfo printerSupportRangeFieldInfo = FieldInfo.ofLabel(FieldInfoCategory.PRINTER_SUPPORT_RANGE, basicInfo.getPrinter());
            if (printerSupportRangeFieldInfo != null) {
                String printerSupportRangeId = printerSupportRangeFieldInfo.getId();
                if (printerSupportRangeId != null) {
                    printerSupportRange = new FieldDTO.Field(printerSupportRangeId);
                }
            }

            FieldDTO.Field projectProgressStep = null;
            String projectProgressStepId = FieldInfo.getIdByCategoryAndLabel(
                    FieldInfoCategory.PROJECT_PROGRESS_STEP,
                    String.valueOf(basicInfo.getProjectStep()));
            if (projectProgressStepId != null) {
                projectProgressStep = new FieldDTO.Field(projectProgressStepId);
            }
/*
            projectInfoDTO = ProjectInfoDTO.builder()
                    .project(project) // 프로젝트
                    .issuetype(new FieldDTO.Field(FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, projectBasicInfo).getId())) // 이슈타입
                    .summary(projectBasicInfo) // 제목
                    .description(description) // 설명
                    .assignee(assignee) // 담당자
                    .customfield_10275(salesManager) // 영업대표
                    //.customfield_10270(basicInfo.getContractor()) // 계약사
                    //.customfield_10271(basicInfo.getClient()) // 고객사
                    //.customfield_10277() // 제품 유형
                    //.customfield_10406() // 제품 정보
                    //.customfield_10408() // 연동 정보
                    .customfield_10272(barcodeType) // 바코드 타입
                    .customfield_10001(team) // 팀
                    .customfield_10279(part) // 파트
                    .customfield_10269(sub_assignee) // 부 담당자
                    // 제품 유형, 제품 정보, 연동 정보 기타
                    .customfield_10415(Arrays.asList(multiOS)) // 멀티 OS
                    .customfield_10247(printerSupportRange) // 프린터 지원 범위
                    .customfield_10411(basicInfo.getProjectName()) // 프로젝트명
                    .customfield_10410(basicInfo.getProjectCode()) // 프로젝트 코드
                    .customfield_10414(String.valueOf(creationDate)) // 프로젝트 배정일
                    //.customfield_10280(projectProgressStep) // 프로젝트 단계
                    .build();
*/

            ProjectInfoDTO.ProjectInfoDTOBuilder projectInfoDTOBuilder = ProjectInfoDTO.builder()
                    .project(project) // 프로젝트
                    .summary(projectBasicInfo) // 제목
                    .description(description); // 설명

            FieldInfo issueTypeFieldInfo = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, projectBasicInfo);
            if (issueTypeFieldInfo != null) {
                projectInfoDTOBuilder.issuetype(new FieldDTO.Field(issueTypeFieldInfo.getId())); // 이슈타입
            }

            if (assignee != null) {
                projectInfoDTOBuilder.assignee(assignee); // 담당자
            }

            if (salesManager != null) {
                projectInfoDTOBuilder.customfield_10275(salesManager); // 영업대표
            }

            if (barcodeType != null) {
                projectInfoDTOBuilder.customfield_10272(barcodeType); // 바코드 타입
            }

            if (team != null) {
                projectInfoDTOBuilder.customfield_10001(team); // 팀
            }

            if (part != null) {
                projectInfoDTOBuilder.customfield_10279(part); // 파트
            }

            if (sub_assignee != null) {
                projectInfoDTOBuilder.customfield_10269(sub_assignee); // 부 담당자
            }

            if (multiOS != null) {
                projectInfoDTOBuilder.customfield_10415(Arrays.asList(multiOS)); // 멀티 OS
            }

            if (printerSupportRange != null) {
                projectInfoDTOBuilder.customfield_10247(printerSupportRange); // 프린터 지원 범위
            }

            if (basicInfo.getProjectName() != null) {
                projectInfoDTOBuilder.customfield_10411(basicInfo.getProjectName()); // 프로젝트명
            }

            if (basicInfo.getProjectCode() != null) {
                projectInfoDTOBuilder.customfield_10410(basicInfo.getProjectCode()); // 프로젝트 코드
            }

            if (creationDate != null) {
                projectInfoDTOBuilder.customfield_10414(String.valueOf(creationDate)); // 프로젝트 배정일
            }

            projectInfoDTO = projectInfoDTOBuilder.build();


            createIssueDTO = new CreateIssueDTO<>(projectInfoDTO);
        } else {
            maintenanceInfoDTO = new MaintenanceInfoDTO();

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

        return true;
    }

    public String createBulkIssue(List<PJ_PG_SUB_Entity> issueList , String jiraProjectId) throws Exception {

        logger.info("나머지 이슈 생성");
        List<PJ_PG_SUB_Entity> nomalIssueList = issueList.subList(1, issueList.size());

        CreateBulkIssueDTO bulkIssueDTO = new CreateBulkIssueDTO();

        List<CreateBulkIssueFieldsDTO> issueUpdates = new ArrayList<>();

        for(PJ_PG_SUB_Entity issueData : nomalIssueList){

            String wssAssignee = getOneAssigneeId(issueData.getWriter());

            String wssContent  = issueData.getIssueContent();
            wssContent = wssContent.replace("<br>", "\n").replace("&nbsp;", " ");
            System.out.println("wssContent : "+wssContent);
            Date wssWriteDate  = issueData.getCreationDate();
            System.out.println("wssWriteDate : "+wssWriteDate);


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
                    .text(wssContent)
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



        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // null 필드 제거 설정
        String json = objectMapper.writeValueAsString(bulkIssueDTO);
        System.out.println(json);

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint ="/rest/api/3/issue/bulk";

        Flux<String> responseFlux = WebClientUtils.postByFlux(webClient,endpoint,bulkIssueDTO,String.class);
        responseFlux.subscribe(response -> {
            System.out.println(response);
        });
        return "0";
    }

    public List<String> getSeveralAssigneeId(String userNames) throws Exception {
        logger.info("[::TransferIssueImpl::] getSeveralAssigneeId");

        if(userNames != null && !userNames.trim().isEmpty()){
            String[] namesArray = userNames.trim().split("\\s*,\\s*");

            List<String> namesArrayList = Arrays.asList(namesArray);

            List<String> userIdList = new ArrayList<>();

            for (String name : namesArrayList) {

                String userId = TB_JIRA_USER_JpaRepository.findByDisplayNameContaining(name).get(0).getAccountId();

                userIdList.add(userId);
            }
            // 앞에 2개의 데이터만 추출하여 반환
            return userIdList.subList(0, Math.min(userIdList.size(), 2));
        }else {
            // 담당자 미지정된 프로젝트 (전자문서사업부 아이디)
            return null;
        }
    }

    public String getOneAssigneeId(String userName) throws Exception{
        logger.info("프로젝트 담당자 이름 아이디 변환");
        List<TB_JIRA_USER_Entity> user = TB_JIRA_USER_JpaRepository.findByDisplayNameContaining(userName);
        if (!user.isEmpty()) {
            String userId = user.get(0).getAccountId();
            return userId;
        } else {
            return null;
        }

    }



}
