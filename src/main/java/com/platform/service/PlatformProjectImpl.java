package com.platform.service;

import com.account.dao.TB_JIRA_USER_JpaRepository;
import com.account.dto.AdminInfoDTO;
import com.account.entity.TB_JIRA_USER_Entity;
import com.account.service.Account;
import com.platform.dto.BaseDTO;
import com.transfer.issue.model.FieldInfo;
import com.transfer.issue.model.FieldInfoCategory;
import com.transfer.issue.model.dto.*;
import com.transfer.issue.service.TransferIssueImpl;
import com.transfer.project.model.dto.CreateProjectDTO;
import com.transfer.project.model.dto.CreateProjectResponseDTO;
import com.transfer.project.service.TransferProjectImpl;
import com.utils.ProjectConfig;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.lang.reflect.Field;
import java.util.*;

@AllArgsConstructor
@Service("platformProject")
public class PlatformProjectImpl implements PlatformProject {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TransferProjectImpl transferProject;

    @Autowired
    private TransferIssueImpl transferIssue;

    @Autowired
    private TB_JIRA_USER_JpaRepository TB_JIRA_USER_JpaRepository;

    @Autowired
    private ProjectConfig projectConfig;

    @Autowired
    private Account account;

    @Override
    public Map<String, String> platformCreateProject(BaseDTO baseDTO) throws Exception {

        BaseDTO.EssentialDTO essentialDTO = baseDTO.getEssential();
        BaseDTO.CommonDTO commonDTO = baseDTO.getCommon();
        BaseDTO.SelectedDTO selectedDTO = baseDTO.getSelected();

        Map<String, String> result = new HashMap<>();

        CreateProjectDTO createProjectDTO = new CreateProjectDTO();

        String jiraProjectKey = transferProject.NamingJiraKey();
        String jiraProjectName = "";

        // 필수
        String projectFlag = essentialDTO.getProjectFlag();
        String projectName = essentialDTO.getProjectName();

        // 선택
        String projectCode = commonDTO.getProjectCode();
        String assignee = commonDTO.getAssignee();
        String subAssignee = commonDTO.getSubAssignee();

        // 담당자 설정
        String assignees = "";
        if (assignee != null && !assignee.trim().isEmpty()) {
            assignees = assignee;
        }
        if (subAssignee != null && !subAssignee.trim().isEmpty()) {
            if (!assignees.isEmpty()) {
                assignees += ",";
            }
            assignees += subAssignee;
        }

        if (projectFlag.equals("P")) { // 프로젝트
            jiraProjectName = "ED-P_" + projectName;
            createProjectDTO.setName(jiraProjectName);
            createProjectDTO.setExistingProjectId(projectConfig.projectTemplate);
        } else { // 유지보수
            jiraProjectName = "ED-M_" + projectName;
            createProjectDTO.setName(jiraProjectName);
            createProjectDTO.setExistingProjectId(projectConfig.maintenanceTemplate);
        }
        createProjectDTO.setKey(jiraProjectKey);

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());

        // 템플릿으로 프로젝트 생성
        String endpoint = "/rest/simplified/latest/project/shared";
        String finalJiraProjectName = jiraProjectName;
        String finalAssignees = assignees;

        result.put("jiraProjectCode", jiraProjectKey);
        result.put("jiraProjectName", finalJiraProjectName);
        try {

            logger.info("[::platformCreateProject::] dto 확인 " + createProjectDTO.toString());
            CreateProjectResponseDTO response = WebClientUtils.post(webClient, endpoint, createProjectDTO, CreateProjectResponseDTO.class)
                    .doOnSuccess(res -> {
                        // 성공적으로 응답을 받았을 때
                        result.put("result", "프로젝트 생성 성공");

                        try {
                            transferProject.saveSuccessData(jiraProjectKey, res.getProjectId(), projectCode, projectName, finalJiraProjectName, projectFlag, finalAssignees); // DB 저장
                            logger.error("[::platformCreateProject::] DB 저장 완료");
                        } catch (Exception e) {
                            logger.error("[::platformCreateProject::] DB 저장 실패");
                        }
                        if (finalAssignees != null && !finalAssignees.trim().isEmpty()) {
                            try {
                                String leader = Arrays.stream(finalAssignees.split(",")).findFirst().orElse(finalAssignees);
                                transferProject.reassignProjectLeader(res.getProjectKey(), leader);
                                logger.info("[::platformCreateProject::] 프로젝트 담당자 지정 성공");
                            } catch (Exception e) {
                                logger.error("[::platformCreateProject::] 프로젝트 담당자 지정 실패");
                            }
                        }
                    })
                    .doOnError(e -> {
                        // 에러 처리
                        logger.info("[::platformCreateProject::] error 발생");
                        result.put("result", "프로젝트 생성 실패");
                    })
                    .block();

            logger.info("[::platformCreateProject::] response -> " + response.toString());
            return result;

        } catch (Exception ex) {
            logger.info("[::platformCreateProject::] try-catch");
            result.put("result", "프로젝트 생성 실패");
            return result;
        }
    }

    @Override
    public Map<String, String> platformCreateProject(String jiraProjectCode, String projectFlag, String projectName, String projectCode, String assignees) throws Exception {

        Map<String, String> result = new HashMap<>();
        CreateProjectDTO createProjectDTO = new CreateProjectDTO();
        String jiraProjectName = "";

        if (projectFlag.equals("P")) { // 프로젝트
            jiraProjectName = "ED-P_" + projectName;
            createProjectDTO.setName(jiraProjectName);
            createProjectDTO.setExistingProjectId(projectConfig.projectTemplate);
        } else { // 유지보수
            jiraProjectName = "ED-M_" + projectName;
            createProjectDTO.setName(jiraProjectName);
            createProjectDTO.setExistingProjectId(projectConfig.maintenanceTemplate);
        }
        createProjectDTO.setKey(jiraProjectCode);

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());

        // 템플릿으로 프로젝트 생성
        String endpoint = "/rest/simplified/latest/project/shared";
        String finalJiraProjectName = jiraProjectName;

        result.put("jiraProjectCode", jiraProjectCode);
        result.put("jiraProjectName", finalJiraProjectName);
        try {

            logger.info("[::platformCreateProject::] dto 확인 " + createProjectDTO.toString());
            CreateProjectResponseDTO response = WebClientUtils.post(webClient, endpoint, createProjectDTO, CreateProjectResponseDTO.class)
                    .doOnSuccess(res -> {
                        // 성공적으로 응답을 받았을 때
                        result.put("result", "프로젝트 생성 성공");

                        try {
                            transferProject.saveSuccessData(jiraProjectCode, res.getProjectId(), projectCode, projectName, finalJiraProjectName, projectFlag, assignees); // DB 저장
                            logger.error("[::platformCreateProject::] DB 저장 완료");
                        } catch (Exception e) {
                            logger.error("[::platformCreateProject::] DB 저장 실패");
                        }
                        if (assignees != null && !assignees.trim().isEmpty()) {
                            try {
                                String leader = Arrays.stream(assignees.split(",")).findFirst().orElse(assignees);
                                transferProject.reassignProjectLeader(res.getProjectKey(), leader);
                                logger.info("[::platformCreateProject::] 프로젝트 담당자 지정 성공");
                            } catch (Exception e) {
                                logger.error("[::platformCreateProject::] 프로젝트 담당자 지정 실패");
                            }
                        }
                    })
                    .doOnError(e -> {
                        // 에러 처리
                        logger.info("[::platformCreateProject::] error 발생");
                        result.put("result", "프로젝트 생성 실패");
                    })
                    .block();

            logger.info("[::platformCreateProject::] response -> " + response.toString());
            return result;

        } catch (Exception ex) {
            logger.info("[::platformCreateProject::] try-catch");
            result.put("result", "프로젝트 생성 실패");
            return result;
        }
    }

    @Override
    public Map<String, String> platformService(BaseDTO baseDTO) throws Exception {

        BaseDTO.EssentialDTO essentialDTO = baseDTO.getEssential();
        BaseDTO.CommonDTO commonDTO = baseDTO.getCommon();
        BaseDTO.SelectedDTO selectedDTO = baseDTO.getSelected();

        logger.info("commonDTO: " + commonDTO.toString());
        logger.info("selectedDTO: " + selectedDTO.toString());

        String jiraProjectCode = transferProject.NamingJiraKey();
        String projectFlag = essentialDTO.getProjectFlag();
        String projectName = essentialDTO.getProjectName();
        String projectCode = commonDTO.getProjectCode();
        String assignee = commonDTO.getAssignee();
        String subAssignee = commonDTO.getSubAssignee();

        // 담당자 설정
        String assignees = setAssignees(assignee, subAssignee);

        Map<String, String> createInfo = platformCreateProject(jiraProjectCode, projectFlag, projectName, projectCode, assignees);

        String createProjectFlag = createInfo.get("result");
        String jiraProjectName = createInfo.get("jiraProjectName");

        if (createProjectFlag.equals("프로젝트 생성 성공")) {

            if (hasValue(commonDTO) || hasValue(selectedDTO)) { // 필수가 아닌 필드에 값이 있는 경우 이슈 생성

                CreateIssueDTO<?> createIssueDTO;
                if (projectFlag.equals("P")) { // 프로젝트 타입

                    ProjectInfoDTO.ProjectInfoDTOBuilder<?, ?> projectBuilder = ProjectInfoDTO.builder();
                    projectBuilder = setCommonFields(projectBuilder, jiraProjectCode, commonDTO);

                    // 이슈타입
                    FieldInfo issueTypeFieldInfo = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, "프로젝트 기본 정보");
                    if (issueTypeFieldInfo != null) {
                        projectBuilder.issuetype(new FieldDTO.Field(issueTypeFieldInfo.getId()));
                    }

                    // 프로젝트 코드
                    if (!projectCode.trim().isEmpty()) {
                        projectBuilder.projectCode(projectCode);
                    }

                    // 프로젝트 이름
                    if (!jiraProjectName.trim().isEmpty()) {
                        projectBuilder.projectName(jiraProjectName);
                    }

                    // 프로젝트 배정일
                    if (!selectedDTO.getProjectAssignmentDate().trim().isEmpty()) {
                        projectBuilder.projectAssignmentDate(selectedDTO.getProjectAssignmentDate());
                    }

                    // 프로젝트 진행 단계
                    FieldInfo projectProgressStepInfo = FieldInfo.ofLabel(FieldInfoCategory.PROJECT_PROGRESS_STEP, selectedDTO.getProjectProgressStep());
                    if (projectProgressStepInfo != null) {
                        projectBuilder.projectProgressStep(new FieldDTO.Field(projectProgressStepInfo.getId()));
                    }

                    ProjectInfoDTO projectInfoDTO = projectBuilder.build();
                    createIssueDTO = new CreateIssueDTO<>(projectInfoDTO);

                } else {

                    MaintenanceInfoDTO.MaintenanceInfoDTOBuilder<?, ?> maintenanceBuilder = MaintenanceInfoDTO.builder();
                    maintenanceBuilder = setCommonFields(maintenanceBuilder, jiraProjectCode, commonDTO);

                    // 이슈타입
                    FieldInfo issueTypeFieldInfo = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, "유지보수 기본 정보");
                    if (issueTypeFieldInfo != null) {
                        maintenanceBuilder.issuetype(new FieldDTO.Field(issueTypeFieldInfo.getId()));
                    }

                    // 프로젝트 코드
                    if (!projectCode.trim().isEmpty()) {
                        maintenanceBuilder.maintenanceCode(projectCode);
                    }

                    // 프로젝트 이름
                    if (!jiraProjectName.trim().isEmpty()) {
                        maintenanceBuilder.maintenanceName(jiraProjectName);
                    }

                    // 계약 여부
                    FieldInfo contractStatusInfo = FieldInfo.ofLabel(FieldInfoCategory.CONTRACT_STATUS, selectedDTO.getContractStatus());
                    if (contractStatusInfo != null) {
                        maintenanceBuilder.contractStatus(new FieldDTO.Field(contractStatusInfo.getId()));
                    }

                    // 유지보수 시작일
                    if (!selectedDTO.getMaintenanceStartDate().trim().isEmpty()) {
                        maintenanceBuilder.maintenanceStartDate(selectedDTO.getMaintenanceStartDate());
                    }

                    // 유지보수 종료일
                    if (!selectedDTO.getMaintenanceEndDate().trim().isEmpty()) {
                        maintenanceBuilder.maintenanceEndDate(selectedDTO.getMaintenanceEndDate());
                    }

                    // 점검 방법
                    FieldInfo inspectionMethodInfo = FieldInfo.ofLabel(FieldInfoCategory.INSPECTION_METHOD, selectedDTO.getInspectionMethod());
                    if (inspectionMethodInfo != null) {
                        maintenanceBuilder.inspectionMethod(new FieldDTO.Field(inspectionMethodInfo.getId()));
                    }

                    // 점검 방법 기타
                    if (!selectedDTO.getInspectionMethodEtc().trim().isEmpty()) {
                        maintenanceBuilder.inspectionMethodEtc(selectedDTO.getInspectionMethodEtc());
                    }

                    // 점검 주기
                    FieldInfo inspectionCycleInfo = FieldInfo.ofLabel(FieldInfoCategory.INSPECTION_CYCLE, selectedDTO.getInspectionCycle());
                    if (inspectionCycleInfo != null) {
                        maintenanceBuilder.inspectionCycle(new FieldDTO.Field(inspectionCycleInfo.getId()));
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

                        System.out.println("[::PlatformProjectImpl::] createBaseInfoIssue -> " + status + " : " + body);
                    }
                }

                // 이슈 생성되었는지 체크 / 이슈 상태를 완료됨으로 변경
                if (responseIssueDTO.getKey() != null) {
                    //relatedProject(wssProjectCode,responseIssueDTO.getKey());
                    transferIssue.changeIssueStatus(responseIssueDTO.getKey());
                    createInfo.put("result2", "이슈 생성 성공");
                } else {
                    createInfo.put("result2", "이슈 생성 실패");
                }

            } else {
                logger.info("[::PlatformProjectImpl::] platformService -> " + "이슈를 생성하기 위한 정보가 없음");
            }
        }

        return createInfo;
    }

    public String setAssignees(String assignee, String subAssignee) {

        String assignees = "";
        if (assignee != null && !assignee.trim().isEmpty()) {
            assignees = assignee;
        }
        if (subAssignee != null && !subAssignee.trim().isEmpty()) {
            if (!assignees.isEmpty()) {
                assignees += ",";
            }
            assignees += subAssignee;
        }

        return assignees;
    }

    public <T> boolean hasValue(T dto) {

        for (Field field : dto.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            try {
                Object value = field.get(dto);
                logger.info("value: " + value);

                if (value instanceof String && !((String) value).trim().isEmpty()) {
                    return true;
                }

            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public <B extends CustomFieldDTO.CustomFieldDTOBuilder<?, ?>> B setCommonFields(B customBuilder, String jiraProjectCode, BaseDTO.CommonDTO commonDTO) throws Exception {

        // 프로젝트
        customBuilder.project(new FieldDTO.Project(jiraProjectCode, null));

        // 제목
        String summary = "기본 정보";
        customBuilder.summary(summary);

        // 담당자 및 부 담당자
        String assignees = setAssignees(commonDTO.getAssignee(), commonDTO.getSubAssignee());
        List<String> assigneeList = transferIssue.getSeveralAssigneeId(assignees);

        if (assigneeList != null) {
            if (assigneeList.size() >= 1) {
                customBuilder.assignee(new FieldDTO.User(assigneeList.get(0)));
            }
            if (assigneeList.size() == 2) {
                customBuilder.subAssignee(new FieldDTO.User(assigneeList.get(1)));
            }
        }

        // 영업대표
        if (!commonDTO.getSalesManager().trim().isEmpty()) {
            if (transferIssue.getSeveralAssigneeId(commonDTO.getSalesManager()) != null && !transferIssue.getSeveralAssigneeId(commonDTO.getSalesManager()).isEmpty()) {
                String salesManagerId = transferIssue.getSeveralAssigneeId(commonDTO.getSalesManager()).get(0);
                if (salesManagerId != null) {
                    customBuilder.salesManager(new FieldDTO.User(salesManagerId));
                }
            }
        }

        // 계약사
        if (!commonDTO.getContractor().isEmpty()) {
            customBuilder.contractor(commonDTO.getContractor());
        }

        // 고객사
        if (!commonDTO.getClient().isEmpty()) {
            customBuilder.contractor(commonDTO.getClient());
        }

        // 제품 정보1
        String productInfo1 = commonDTO.getProductInfo1();
        if (!productInfo1.trim().isEmpty()) {
            Optional.ofNullable(setProductInfo(FieldInfoCategory.PRODUCT_INFO1, productInfo1))
                    .ifPresent(productInfoList -> customBuilder.productInfo1(productInfoList));

        }

        // 제품 정보2
        String productInfo2 = commonDTO.getProductInfo2();
        if (!productInfo2.trim().isEmpty()) {
            Optional.ofNullable(setProductInfo(FieldInfoCategory.PRODUCT_INFO2, productInfo2))
                    .ifPresent(productInfoList -> customBuilder.productInfo2(productInfoList));

        }

        // 제품 정보3
        String productInfo3 = commonDTO.getProductInfo3();
        if (!productInfo3.trim().isEmpty()) {
            Optional.ofNullable(setProductInfo(FieldInfoCategory.PRODUCT_INFO3, productInfo3))
                    .ifPresent(productInfoList -> customBuilder.productInfo3(productInfoList));

        }

        // 제품 정보4
        String productInfo4 = commonDTO.getProductInfo4();
        if (!productInfo4.trim().isEmpty()) {
            Optional.ofNullable(setProductInfo(FieldInfoCategory.PRODUCT_INFO4, productInfo4))
                    .ifPresent(productInfoList -> customBuilder.productInfo4(productInfoList));

        }

        // 제품 정보5
        String productInfo5 = commonDTO.getProductInfo5();
        if (!productInfo5.trim().isEmpty()) {
            Optional.ofNullable(setProductInfo(FieldInfoCategory.PRODUCT_INFO5, productInfo5))
                    .ifPresent(productInfoList -> customBuilder.productInfo5(productInfoList));

        }

        // 바코드 타입
        FieldInfo barcodeTypeInfo = FieldInfo.ofLabel(FieldInfoCategory.BARCODE_TYPE, String.valueOf(commonDTO.getBarcodeType()));
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
        FieldInfo multiOsInfo = FieldInfo.ofLabel(FieldInfoCategory.OS, commonDTO.getMultiOsSupport());
        if (multiOsInfo != null) {
            customBuilder.multiOsSupport(Arrays.asList(new FieldDTO.Field(multiOsInfo.getId())));

        }

        // 프린터 지원 범위
        FieldInfo printerSupportRangeInfo = FieldInfo.ofLabel(FieldInfoCategory.PRINTER_SUPPORT_RANGE, commonDTO.getPrinterSupportRange());
        if (printerSupportRangeInfo != null) {
            customBuilder.printerSupportRange(new FieldDTO.Field(printerSupportRangeInfo.getId()));
        }

        // 설명
        if (!commonDTO.getDescription().trim().isEmpty()) {
            customBuilder.description(transferIssue.setDescription(Collections.singletonList(commonDTO.getDescription())));
        }

        return customBuilder;
    }

    public List<FieldDTO.Field> setProductInfo(String category, String info) {

        String[] productList = info.split(",");
        List<FieldDTO.Field> productInfoList = new ArrayList<>();

        for (String product : productList) {
            FieldInfo productInfo = FieldInfo.ofLabel(category, product);
            if (productInfo != null) {
                productInfoList.add(new FieldDTO.Field(productInfo.getId()));
            }
        }

        if (productInfoList.isEmpty()) {
            return null;
        }

        return productInfoList;
    }
}
