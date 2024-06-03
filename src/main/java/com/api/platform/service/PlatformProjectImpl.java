package com.api.platform.service;

import com.api.platform.dto.BaseDTO;
import com.api.platform.dto.ReturnMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jira.account.model.dao.TB_JIRA_USER_JpaRepository;
import com.jira.account.model.entity.TB_JIRA_USER_Entity;
import com.jira.account.service.AccountImpl;
import com.jira.issue.model.FieldInfo;
import com.jira.issue.model.FieldInfoCategory;
import com.jira.issue.model.dto.FieldDTO;
import com.jira.issue.model.dto.ResponseIssueDTO;
import com.jira.issue.model.dto.create.CreateIssueDTO;
import com.jira.issue.model.dto.create.CustomFieldDTO;
import com.jira.issue.model.dto.create.MaintenanceInfoDTO;
import com.jira.issue.model.dto.create.ProjectInfoDTO;
import com.jira.issue.model.dto.search.SearchIssueDTO;
import com.jira.issue.model.dto.search.SearchMaintenanceInfoDTO;
import com.jira.issue.model.dto.search.SearchProjectInfoDTO;
import com.jira.issue.model.dto.weblink.RequestWeblinkDTO;
import com.jira.issue.service.JiraIssueImpl;
import com.jira.project.model.dao.TB_JLL_JpaRepository;
import com.jira.project.model.dao.TB_JML_JpaRepository;
import com.jira.project.model.dto.CreateProjectDTO;
import com.jira.project.model.dto.CreateProjectResponseDTO;
import com.jira.project.model.dto.ProjectDTO;
import com.jira.project.model.entity.TB_JLL_Entity;
import com.jira.project.model.entity.TB_JML_Entity;
import com.jira.project.service.JiraProjectImpl;
import com.utils.ProjectConfig;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor
@Service("platformProject")
public class PlatformProjectImpl implements PlatformProject {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JiraProjectImpl jiraProject;

    @Autowired
    private JiraIssueImpl jiraIssue;

    @Autowired
    private AccountImpl account;

    @Autowired
    private TB_JIRA_USER_JpaRepository TB_JIRA_USER_JpaRepository;

    @Autowired
    private TB_JLL_JpaRepository TB_JLL_JpaRepository;

    @Autowired
    private  TB_JML_JpaRepository TB_JML_JpaRepository;

    @Autowired
    private ProjectConfig projectConfig;

    @Autowired
    private WebClientUtils webClientUtils;

    @Override
    public Map<String, String> platformCreateProject(BaseDTO baseDTO) throws Exception {

        BaseDTO.EssentialDTO essentialDTO = baseDTO.getEssential();
        BaseDTO.CommonDTO commonDTO = baseDTO.getCommon();
        BaseDTO.SelectedDTO selectedDTO = baseDTO.getSelected();

        Map<String, String> result = new HashMap<>();

        CreateProjectDTO createProjectDTO = new CreateProjectDTO();

        String jiraProjectKey = jiraProject.namingJiraKey();
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

        // 템플릿으로 프로젝트 생성
        String endpoint = "/rest/simplified/latest/project/shared";
        String finalJiraProjectName = jiraProjectName;
        String finalAssignees = assignees;

        result.put("jiraProjectCode", jiraProjectKey);
        result.put("jiraProjectName", finalJiraProjectName);
        try {

            logger.info("[::platformCreateProject::] dto 확인 " + createProjectDTO);
            CreateProjectResponseDTO response = webClientUtils.post(endpoint, createProjectDTO, CreateProjectResponseDTO.class)
                    .doOnSuccess(res -> {
                        // 성공적으로 응답을 받았을 때
                        result.put("result", "프로젝트 생성 성공");

                        try {
                            jiraProject.saveSuccessData(jiraProjectKey, res.getProjectId(), projectCode, projectName, finalJiraProjectName, projectFlag, finalAssignees); // DB 저장
                            logger.error("[::platformCreateProject::] DB 저장 완료");
                        } catch (Exception e) {
                            logger.error("[::platformCreateProject::] DB 저장 실패");
                        }
                        if (finalAssignees != null && !finalAssignees.trim().isEmpty()) {
                            try {
                                String leader = Arrays.stream(finalAssignees.split(",")).findFirst().orElse(finalAssignees);
                                jiraProject.reassignProjectLeader(res.getProjectKey(), leader);
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

    /*
    *  플랫폼으로 수정하기위해 프로젝트 데이터 조회
    * */
    @Override
    public BaseDTO platformGetProject(String projectType, String jiraKey) throws Exception {
        // 지라 프로젝트 정보 조회
        String 기본정보이슈_키;

        String 이슈_유형_키;

        BaseDTO 기본정보_이슈 = new BaseDTO();

        ProjectDTO 프로젝트 = jiraProject.getJiraProjectInfoByJiraKey(jiraKey); // 지라에서 조회한 프로젝트

        String 프로젝트_담당자 = 프로젝트.getLead().getDisplayName();


        // 기본정보 이슈 조회
        if( projectType.equals("M")){
            이슈_유형_키 = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, "유지보수 기본 정보").getId();

            기본정보이슈_키 = jiraIssue.getBaseIssueKey(jiraKey,이슈_유형_키); // 해당 프로젝트 기본정보 이슈키 조회
            SearchIssueDTO<SearchMaintenanceInfoDTO> 유지보수_기본정보이슈 = jiraIssue.getMaintenanceIssue(기본정보이슈_키);

            /*String 프로젝트_이름 = 유지보수_기본정보이슈.getFields().getMaintenanceName();
            String 유지보수_코드 = 유지보수_기본정보이슈.getFields().getMaintenanceCode();
            String 영업_대표 = 유지보수_기본정보이슈.getFields().getSalesManager().getDisplayName();

            // 필수 항목: 프로젝트 이름, 타입
            BaseDTO.EssentialDTO 필수항목 = BaseDTO.EssentialDTO.builder()
                    .projectFlag("M")
                    .projectName(프로젝트_이름)
                    .build();
            기본정보_이슈.setEssential(필수항목);

            // 선택 항목:
            BaseDTO.CommonDTO 선택항목 = BaseDTO.CommonDTO.builder()
                    .projectCode(유지보수_코드)
                    .assignee(프로젝트_담당자)
                    .salesManager(영업_대표)
                    .build();
            기본정보_이슈.setCommon(선택항목);

            기본정보_이슈.setSelected();*/


        }else{
            이슈_유형_키 = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, "프로젝트 기본 정보").getId();

            기본정보이슈_키 = jiraIssue.getBaseIssueKey(jiraKey,이슈_유형_키);
            SearchIssueDTO<SearchProjectInfoDTO> 프로젝트_기본정보 = jiraIssue.getProjectIssue(기본정보이슈_키);
        }

        return null;
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

        // 템플릿으로 프로젝트 생성
        String endpoint = "/rest/simplified/latest/project/shared";
        String finalJiraProjectName = jiraProjectName;

        result.put("jiraProjectCode", jiraProjectCode);
        result.put("jiraProjectName", finalJiraProjectName);
        try {

            logger.info("[::platformCreateProject::] dto 확인 " + createProjectDTO.toString());
            CreateProjectResponseDTO response = webClientUtils.post(endpoint, createProjectDTO, CreateProjectResponseDTO.class)
                    .doOnSuccess(res -> {
                        // 성공적으로 응답을 받았을 때
                        result.put("result", "프로젝트 생성 성공");

                        try {
                            jiraProject.saveSuccessData(jiraProjectCode, res.getProjectId(), projectCode, projectName, finalJiraProjectName, projectFlag, assignees); // DB 저장
                            logger.error("[::platformCreateProject::] DB 저장 완료");
                        } catch (Exception e) {
                            logger.error("[::platformCreateProject::] DB 저장 실패");
                        }
                        if (assignees != null && !assignees.trim().isEmpty()) {
                            try {
                                String leader = Arrays.stream(assignees.split(",")).findFirst().orElse(assignees);
                                jiraProject.reassignProjectLeader(res.getProjectKey(), leader);
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

        // TODO: RequestBody 포맷이 잘못된 경우 등 에러 발생 시, 에러 메시지 리턴 처리 필요
        BaseDTO.EssentialDTO essentialDTO = baseDTO.getEssential();
        BaseDTO.CommonDTO commonDTO = baseDTO.getCommon();
        BaseDTO.SelectedDTO selectedDTO = baseDTO.getSelected();

        logger.info("commonDTO: " + commonDTO.toString());
        logger.info("selectedDTO: " + selectedDTO.toString());

        String jiraProjectCode = jiraProject.namingJiraKey();
        String projectFlag = essentialDTO.getProjectFlag();
        String projectName = essentialDTO.getProjectName().trim();
        String projectCode = commonDTO.getProjectCode();
        String assignee = commonDTO.getAssignee();
        String subAssignee = commonDTO.getSubAssignee();
        String description = commonDTO.getDescription();

        String salesManager = commonDTO.getSalesManager(); // 영업 담당자
        // 담당자 설정
        String assignees = setAssignees(assignee, subAssignee);
        // 프로젝트 생성
        Map<String, String> createInfo = platformCreateProject(jiraProjectCode, projectFlag, projectName, projectCode, assignees);

        String createProjectFlag = createInfo.get("result");
        String jiraProjectName = createInfo.get("jiraProjectName");

        if (createProjectFlag.equals("프로젝트 생성 성공")) {

            CreateIssueDTO<?> createIssueDTO;
            if (projectFlag.equals("P")) { // 프로젝트 타입

                if(commonDTO.getAllocationFlag()){
                    createStaffAllocationIssue(description,projectName,projectFlag,salesManager);
                }

                ProjectInfoDTO.ProjectInfoDTOBuilder<?, ?> projectBuilder = ProjectInfoDTO.builder();
                projectBuilder = setCommonFields(projectBuilder, jiraProjectCode, commonDTO);

                // 제목
                setBuilder("[P] " + projectName, projectBuilder::summary);

                // 이슈타입
                setBuilder(
                        () -> FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, "프로젝트 기본 정보"),
                        fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                        projectBuilder::issuetype
                );

                // 프로젝트 코드
                setBuilder(projectCode, projectBuilder::projectCode);

                // 프로젝트 이름
                setBuilder(projectName, projectBuilder::projectName);

                // 프로젝트 배정일
                setBuilder(selectedDTO::getProjectAssignmentDate, projectBuilder::projectAssignmentDate);

                // 프로젝트 진행 단계
                setBuilder(
                        () -> FieldInfo.ofLabel(FieldInfoCategory.PROJECT_PROGRESS_STEP, selectedDTO.getProjectProgressStep()),
                        fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                        projectBuilder::projectProgressStep
                );

                ProjectInfoDTO projectInfoDTO = projectBuilder.build();
                createIssueDTO = new CreateIssueDTO<>(projectInfoDTO);

            }
            else {

                if(commonDTO.getAllocationFlag()){
                    createStaffAllocationIssue(description,projectName,projectFlag,salesManager);
                }

                MaintenanceInfoDTO.MaintenanceInfoDTOBuilder<?, ?> maintenanceBuilder = MaintenanceInfoDTO.builder();
                maintenanceBuilder = setCommonFields(maintenanceBuilder, jiraProjectCode, commonDTO);

                // 제목
                setBuilder("[M] " + projectName, maintenanceBuilder::summary);

                // 이슈타입
                setBuilder(
                        () -> FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, "유지보수 기본 정보"),
                        fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                        maintenanceBuilder::issuetype
                );

                // 프로젝트 코드
                setBuilder(projectCode, maintenanceBuilder::maintenanceCode);

                // 프로젝트 이름
                setBuilder(projectName, maintenanceBuilder::maintenanceName);

                // 계약 여부
                setBuilder(
                        () -> FieldInfo.ofLabel(FieldInfoCategory.CONTRACT_STATUS, selectedDTO.getContractStatus()),
                        fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                        maintenanceBuilder::contractStatus
                );

                // 유지보수 시작일
                setBuilder(selectedDTO::getMaintenanceStartDate, maintenanceBuilder::maintenanceStartDate);

                // 유지보수 종료일
                setBuilder(selectedDTO::getMaintenanceEndDate, maintenanceBuilder::maintenanceEndDate);

                // 점검 방법
                setBuilder(
                        () -> FieldInfo.ofLabel(FieldInfoCategory.INSPECTION_METHOD, selectedDTO.getInspectionMethod()),
                        fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                        maintenanceBuilder::inspectionMethod
                );

                // 점검 방법 기타
                setBuilder(selectedDTO::getInspectionMethodEtc, maintenanceBuilder::inspectionMethodEtc);

                // 점검 주기
                setBuilder(
                        () -> FieldInfo.ofLabel(FieldInfoCategory.INSPECTION_CYCLE, selectedDTO.getInspectionCycle()),
                        fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                        maintenanceBuilder::inspectionCycle
                );

                MaintenanceInfoDTO maintenanceInfoDTO = maintenanceBuilder.build();
                createIssueDTO = new CreateIssueDTO<>(maintenanceInfoDTO);
            }

            // 이슈 생성
            String endpoint = "/rest/api/3/issue";
            ResponseIssueDTO responseIssueDTO = null;
            try {
                responseIssueDTO = webClientUtils.post(endpoint, createIssueDTO, ResponseIssueDTO.class).block();


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
                jiraIssue.changeIssueStatus(responseIssueDTO.getKey());
                createInfo.put("result2", "이슈 생성 성공");
            } else {
                createInfo.put("result2", "이슈 생성 실패");
            }

        }

        return createInfo;
    }

    @Override
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

    @Override
    @Transactional
    public void upDateProjectInfo(BaseDTO baseDTO) throws Exception {
        // 프로젝트 정보 업데이트
        CreateProjectDTO 업데이트_정보 = new CreateProjectDTO();
        // 프로젝트 이름
        String 프로젝트_이름 = baseDTO.getEssential().getProjectName();
        // 담당자
        String 담당자_이름 =baseDTO.getCommon().getAssignee();

        if(담당자_이름 != null && !담당자_이름.isEmpty()){
            TB_JIRA_USER_Entity user = (TB_JIRA_USER_Entity) TB_JIRA_USER_JpaRepository.findByDisplayNameContaining(담당자_이름);
            String 계정_정보 = user.getAccountId();
            업데이트_정보.setLeadAccountId(계정_정보);
        }

        업데이트_정보.setName(프로젝트_이름);

        jiraProject.updateProjectInfo(업데이트_정보);

        // 기본 정보 이슈 업데이트

    }

    @Override
    public Map<String, String> updateBaseIssue(String issueKey, BaseDTO baseDTO) throws Exception {

        logger.info("[ :: PlatformProjectImpl :: ] updateBaseIssue -> " + issueKey);

        Map<String, String> result = new HashMap<>();

        CreateIssueDTO<?> updateIssueDTO = setIssueDTO(StringUtils.EMPTY, baseDTO);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String jsonRequestBody = objectMapper.writeValueAsString(updateIssueDTO);
        logger.info("[ :: PlatformProjectImpl :: ] updateIssueDTO -> " + jsonRequestBody);

        // 이슈 업데이트
        if (issueKey != null) {
            String endpoint = "/rest/api/3/issue/" + issueKey;
            Optional<Boolean> response = webClientUtils.executePut(endpoint, updateIssueDTO);
            if (response.isPresent()) {
                if (response.get()) {
                    result.put("jiraIssueKey", issueKey);
                    result.put("result", "이슈 업데이트 성공");

                    return result;
                }
            }
        }

        result.put("jiraIssueKey", issueKey);
        result.put("result", "이슈 업데이트 실패");

        return result;
    }

    public CreateIssueDTO setIssueDTO(String jiraProjectCode, BaseDTO baseDTO) throws Exception {

        BaseDTO.EssentialDTO essentialDTO = baseDTO.getEssential();
        BaseDTO.CommonDTO commonDTO = baseDTO.getCommon();
        BaseDTO.SelectedDTO selectedDTO = baseDTO.getSelected();

        String projectName = Optional.ofNullable(essentialDTO.getProjectName())
                .map(String::trim)
                .orElse(null);

        CreateIssueDTO<?> issueDTO = null;
        if (StringUtils.equals(essentialDTO.getProjectFlag(), "P")) {

            ProjectInfoDTO.ProjectInfoDTOBuilder<?, ?> projectBuilder = ProjectInfoDTO.builder();
            projectBuilder = setCommonFields(projectBuilder, jiraProjectCode, commonDTO);

            // 제목
            if (projectName != null) {
                setBuilder("[P] " + projectName, projectBuilder::summary);
            }

            // 이슈타입
            setBuilder(
                    () -> FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, "프로젝트 기본 정보"),
                    fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                    projectBuilder::issuetype
            );

            // 프로젝트 코드
            setBuilder(commonDTO.getProjectCode(), projectBuilder::projectCode);

            // 프로젝트 이름
            setBuilder(projectName, projectBuilder::projectName);

            // 프로젝트 배정일
            setBuilder(selectedDTO::getProjectAssignmentDate, projectBuilder::projectAssignmentDate);

            // 프로젝트 진행 단계
            setBuilder(
                    () -> FieldInfo.ofLabel(FieldInfoCategory.PROJECT_PROGRESS_STEP, selectedDTO.getProjectProgressStep()),
                    fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                    projectBuilder::projectProgressStep
            );

            ProjectInfoDTO projectInfoDTO = projectBuilder.build();
            issueDTO = new CreateIssueDTO<>(projectInfoDTO);

        }
        else {

            MaintenanceInfoDTO.MaintenanceInfoDTOBuilder<?, ?> maintenanceBuilder = MaintenanceInfoDTO.builder();
            maintenanceBuilder = setCommonFields(maintenanceBuilder, jiraProjectCode, commonDTO);

            // 제목
            if (projectName != null) {
                setBuilder("[M] " + projectName, maintenanceBuilder::summary);
            }

            // 이슈타입
            setBuilder(
                    () -> FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, "유지보수 기본 정보"),
                    fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                    maintenanceBuilder::issuetype
            );

            // 프로젝트 코드
            setBuilder(commonDTO.getProjectCode(), maintenanceBuilder::maintenanceCode);

            // 프로젝트 이름
            setBuilder(projectName, maintenanceBuilder::maintenanceName);

            // 계약 여부
            setBuilder(
                    () -> FieldInfo.ofLabel(FieldInfoCategory.CONTRACT_STATUS, selectedDTO.getContractStatus()),
                    fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                    maintenanceBuilder::contractStatus
            );

            // 유지보수 시작일
            setBuilder(selectedDTO::getMaintenanceStartDate, maintenanceBuilder::maintenanceStartDate);

            // 유지보수 종료일
            setBuilder(selectedDTO::getMaintenanceEndDate, maintenanceBuilder::maintenanceEndDate);

            // 점검 방법
            setBuilder(
                    () -> FieldInfo.ofLabel(FieldInfoCategory.INSPECTION_METHOD, selectedDTO.getInspectionMethod()),
                    fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                    maintenanceBuilder::inspectionMethod
            );

            // 점검 방법 기타
            setBuilder(selectedDTO::getInspectionMethodEtc, maintenanceBuilder::inspectionMethodEtc);

            // 점검 주기
            setBuilder(
                    () -> FieldInfo.ofLabel(FieldInfoCategory.INSPECTION_CYCLE, selectedDTO.getInspectionCycle()),
                    fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                    maintenanceBuilder::inspectionCycle
            );

            MaintenanceInfoDTO maintenanceInfoDTO = maintenanceBuilder.build();
            issueDTO = new CreateIssueDTO<>(maintenanceInfoDTO);
        }

        return issueDTO;
    }

    /*
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
    */

    @Override
    public <B extends CustomFieldDTO.CustomFieldDTOBuilder<?, ?>> B setCommonFields(B customBuilder, String jiraProjectCode, BaseDTO.CommonDTO commonDTO) throws Exception {

        // 프로젝트
        if (org.springframework.util.StringUtils.hasText(jiraProjectCode)) {
            logger.info("프로젝트 코드: " + jiraProjectCode);
            customBuilder.project(new FieldDTO.Project(jiraProjectCode, null));
        }

        // 담당자
        String 담당자 = getAccountId(commonDTO.getAssignee());
        setBuilder(
                () -> 담당자,
                accountId -> new FieldDTO.User(accountId),
                customBuilder::assignee
        );

        // 부 담당자
        setBuilder(
                () -> getAccountId(commonDTO.getSubAssignee()),
                accountId -> new FieldDTO.User(accountId),
                customBuilder::subAssignee
        );

        // 영업대표
        setBuilder(
                () -> getAccountId(commonDTO.getSalesManager()),
                accountId -> new FieldDTO.User(accountId),
                customBuilder::salesManager
        );

        // 계약사
        setBuilder(commonDTO::getContractor, customBuilder::contractor);

        // 고객사
        setBuilder(commonDTO::getClient, customBuilder::client);

        // 제품 정보1
        setBuilder(
                () -> setProductInfo(FieldInfoCategory.PRODUCT_INFO1, commonDTO.getProductInfo1()),
                customBuilder::productInfo1
        );

        // 제품 정보2
        setBuilder(
                () -> setProductInfo(FieldInfoCategory.PRODUCT_INFO2, commonDTO.getProductInfo2()),
                customBuilder::productInfo2
        );

        // 제품 정보3
        setBuilder(
                () -> setProductInfo(FieldInfoCategory.PRODUCT_INFO3, commonDTO.getProductInfo3()),
                customBuilder::productInfo3
        );

        // 제품 정보4
        setBuilder(
                () -> setProductInfo(FieldInfoCategory.PRODUCT_INFO4, commonDTO.getProductInfo4()),
                customBuilder::productInfo4
        );

        // 제품 정보5
        setBuilder(
                () -> setProductInfo(FieldInfoCategory.PRODUCT_INFO5, commonDTO.getProductInfo5()),
                customBuilder::productInfo5
        );

        // 바코드 타입
        setBuilder(
                () -> FieldInfo.ofLabel(FieldInfoCategory.BARCODE_TYPE, commonDTO.getBarcodeType()),
                fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                customBuilder::barcodeType
        );

        // 팀, 파트
        if (담당자 != null) {
            Optional<TB_JIRA_USER_Entity> userEntity = Optional.ofNullable(TB_JIRA_USER_JpaRepository.findByAccountId(담당자));

            userEntity.ifPresent(사용자_정보 -> {
                setBuilder(
                        () -> FieldInfo.ofLabel(FieldInfoCategory.TEAM, 사용자_정보.getTeam()),
                        fieldInfo -> fieldInfo.getId(),
                        customBuilder::team
                );

                setBuilder(
                        () -> FieldInfo.ofLabel(FieldInfoCategory.PART, 사용자_정보.getPart()),
                        fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                        customBuilder::part
                );
            });
        }

        // 멀티 OS
        setBuilder(
                () -> FieldInfo.ofLabel(FieldInfoCategory.OS, commonDTO.getMultiOsSupport()),
                fieldInfo -> Arrays.asList(new FieldDTO.Field(fieldInfo.getId())),
                customBuilder::multiOsSupport
        );

        // 프린터 지원 범위
        setBuilder(
                () -> FieldInfo.ofLabel(FieldInfoCategory.PRINTER_SUPPORT_RANGE, commonDTO.getPrinterSupportRange()),
                fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                customBuilder::printerSupportRange
        );

        // 설명
        String 설명 = Optional.ofNullable(commonDTO.getDescription())
                .map(String::trim)
                .orElse(StringUtils.EMPTY);

        if (!StringUtils.isEmpty(설명)) {

            설명 = processHtml(설명);
            String text = replaceText(removeHtmlTags(Jsoup.clean(설명, Whitelist.basic())), "&nbsp;", " ");

            FieldDTO.ContentItem contentItem = FieldDTO.ContentItem.builder().text(text).type("text").build();

            List<FieldDTO.ContentItem> contentItems = new ArrayList<>();
            contentItems.add(contentItem);

            FieldDTO.Content content = FieldDTO.Content.builder().type("paragraph").content(contentItems).build();
            List<FieldDTO.Content> contents = new ArrayList<>();
            contents.add(content);

            FieldDTO.Description description = FieldDTO.Description.builder().version(1).type("doc").content(contents).build();
            customBuilder.description(description);
        }

        // 기타 정보
        String 기타 = Optional.ofNullable(commonDTO.getEtc())
                .map(String::trim)
                .orElse(StringUtils.EMPTY);

        if (!StringUtils.isEmpty(기타)) {
            customBuilder.etc(jiraIssue.setDescription(Collections.singletonList(commonDTO.getEtc())));
        }

        return customBuilder;
    }

    public static String removeHtmlTags(String value) {
        String rtnVal = org.apache.commons.text.StringEscapeUtils.unescapeHtml4(value);
        // unescapeHtml : <p><h3 class="kkk">테스트</p> 케이스

        // https://www.baeldung.com/java-remove-html-tags
        rtnVal = rtnVal.replaceAll("<[^>]*>", "");
        //  태그제거 : 테스트 케이스

        // bullet 문자열
        rtnVal = rtnVal.replace("•", "__BULLET__");

        rtnVal = org.apache.commons.text.StringEscapeUtils.escapeHtml4(rtnVal);
        // escapeHtml : 테스트&nbsp;케이스

        // bullet 문자열 처리
        rtnVal = rtnVal.replace("__BULLET__", "•");

        return rtnVal;
    }

    public static String replaceText(String text, String originTxt, String replaceTxt) {
        return text.replaceAll(originTxt, replaceTxt);
    }

    public static String processHtml(String html) {
        Document doc = Jsoup.parse(html);

        // ol 태그 처리
        Elements olElements = doc.select("ol");
        for (Element ol : olElements) {
            processOlElement(ol);
        }

        // ul 태그 처리
        Elements ulElements = doc.select("ul");
        for (Element ul : ulElements) {
            processUlElement(ul);
        }

        return doc.body().html();
    }

    public static void processOlElement(Element ol) {
        int counter = 1;
        char indentCounter = 'a';

        Elements liElements = ol.children();
        for (Element li : liElements) {
            if (li.hasClass("ql-indent-1")) {
                li.prepend("&nbsp;&nbsp;" + indentCounter++ + ". ");
            } else {
                li.prepend(counter++ + ". ");
                indentCounter = 'a'; // 새 li 만나면 초기화
            }
        }
    }

    public static void processUlElement(Element ul) {
        char bullet = '•';

        Elements liElements = ul.children();
        for (Element li : liElements) {
            if (li.hasClass("ql-indent-1")) {
                li.prepend("&nbsp;&nbsp;" + bullet + " ");
            } else {
                li.prepend(bullet + " ");
            }
        }
    }

    @Override
    public List<FieldDTO.Field> setProductInfo(String category, List<String> productList) {

        if (productList == null || productList.isEmpty()) {
            return Collections.emptyList(); // 값이 없는 경우 빈 리스트 반환
        }

        List<FieldDTO.Field> productInfoList = new ArrayList<>();
        for (String product : productList) {
            FieldInfo productInfo = FieldInfo.ofLabel(category, product);
            if (productInfo != null) {
                productInfoList.add(new FieldDTO.Field(productInfo.getId()));
            }
        }

        return productInfoList;
    }

    @Override
    public void setBuilder(String info, Consumer<String> consumer) {
        if (info != null && !info.trim().isEmpty()) {
            consumer.accept(info);
        }
    }

    @Override
    public <T> void setBuilder(Supplier<T> supplier, Consumer<T> consumer) {
        T info = supplier.get();
        if (info instanceof String && !((String) info).isEmpty()) {
            consumer.accept(info);
        } else if (info instanceof List && !((List<?>) info).isEmpty()) {
            consumer.accept(info);
        }
    }

    @Override
    public <T, R> void setBuilder(Supplier<T> supplier, Function<T, R> function, Consumer<R> consumer) {
        T result = supplier.get();
        if (result != null) {
            R info = function.apply(result);
            consumer.accept(info);
        }
    }

    @Override
    @Transactional
    public ReturnMessage platformWeblink(String mainJiraKey, String subJiraKey) throws Exception{

        // 프로젝트 연관 관계 설정 및 디비 저장
        ReturnMessage returnMessage = new ReturnMessage();
        List<String> messages = new ArrayList<>();
        String errorMessage;

        //비정상 정보가 들어왔을 때 에러처리
        if(mainJiraKey == null || subJiraKey == null || mainJiraKey.equals(subJiraKey)){
            errorMessage = "웹링크 연결에 실패 하였습니다.";
            messages.add(errorMessage);
            returnMessage.setErrorMessages(messages);
            returnMessage.setResult(false);
        }

        try {
            TB_JLL_Entity entity = TB_JLL_Entity.builder().parentKey(mainJiraKey).childKey(subJiraKey).linkCheckFlag(false).build(); // 최초 연결 관리 정보 입력

            TB_JLL_Entity saveInfoEntity  = TB_JLL_JpaRepository.save(entity);

            if(saveInfoEntity  != null){
                String parentKey =  saveInfoEntity.getParentKey();
                String childKey = saveInfoEntity.getChildKey();

                // 프로젝트 기본정보
                TB_JML_Entity parentInfo = TB_JML_JpaRepository.findByKey(parentKey);
                TB_JML_Entity childInfo = TB_JML_JpaRepository.findByKey(childKey);

                // 지라 기본정보 이슈 키 조회
                String parentBaseIssueKey = jiraIssue.getBaseIssueKeyByJiraKey(parentKey);
                String childBaseIssueKey = jiraIssue.getBaseIssueKeyByJiraKey(childKey);

                if( parentBaseIssueKey != null && childBaseIssueKey !=null){ // 양 방향 연결 가능한 경우

                    RequestWeblinkDTO parenetWeblink  = new RequestWeblinkDTO();
                    parenetWeblink.setIssueIdOrKey(parentBaseIssueKey);
                    parenetWeblink.setJiraKey(childInfo.getKey());
                    parenetWeblink.setTitle(childInfo.getWssProjectName());

                    jiraIssue.createWebLink(parenetWeblink);

                    RequestWeblinkDTO childWeblink  = new RequestWeblinkDTO();
                    childWeblink.setIssueIdOrKey(childBaseIssueKey);
                    childWeblink.setJiraKey(parentInfo.getKey());
                    childWeblink.setTitle(parentInfo.getWssProjectName());

                    jiraIssue.createWebLink(childWeblink);

                    TB_JLL_Entity jllEntity = TB_JLL_JpaRepository.findByParentKeyAndChildKey(mainJiraKey, subJiraKey);
                    jllEntity.setLinkCheckFlag(true);
                    TB_JLL_Entity saveFlagEntity = TB_JLL_JpaRepository.save(jllEntity);

                    String message = "프로젝트 "+parentKey+"와 "+childInfo+"에 웹링크 생성 완료되었습니다.";

                    returnMessage.setResultMessage(message);
                    returnMessage.setResult(true);

                }else {// 기본 정보 이슈가 없는 경우 오류
                    if(childBaseIssueKey == null){
                        errorMessage = "프로젝트 "+ childKey+"에 생성된 기본정보 이슈가 없습니다";
                    }else if(parentBaseIssueKey == null){
                        errorMessage = "프로젝트 "+ parentInfo+"에 생성된 기본정보 이슈가 없습니다";
                    }else{
                        errorMessage = "프로젝트 "+ parentInfo+" 와 "+childKey +"에 생성된 기본정보 이슈가 없습니다";
                    }
                    messages.add(errorMessage);
                    returnMessage.setErrorMessages(messages);
                    returnMessage.setResult(false);
                }
            }else{
                errorMessage = "웹링크 연결에 실패 하였습니다.";
                messages.add(errorMessage);
                returnMessage.setErrorMessages(messages);
                returnMessage.setResult(false);
            }
        }catch (Exception e){
            errorMessage = "웹링크 연결에 실패 하였습니다.";
            messages.add(errorMessage);
            returnMessage.setErrorMessages(messages);
            returnMessage.setResult(false);
        }


        return returnMessage;
    }

    private void createStaffAllocationIssue(String description, String projectName, String flag, String salesManager)throws Exception{

        logger.info("[::platformCreateProject::] 인력배정 이슈 생성 시작");

        CreateIssueDTO<?> createIssueDTO;
        FieldDTO fieldDTO = new FieldDTO();

        // 담당자
        FieldDTO.User user = FieldDTO.User.builder()
                .accountId("63e5a5e5614cb4ba5303f921").build(); // 임팀장님 디폴트
        fieldDTO.setAssignee(user);

        // 프로젝트 아이디
        String projectCode;
        String issueTypeCode;
        if(flag.equals("P")){
            projectCode = "13583";
            issueTypeCode = "10685";
        }else{
            projectCode = "13584";
            issueTypeCode = "10688";
        }
        FieldDTO.Project project = FieldDTO.Project.builder()
                .id(projectCode)
                .build();
        fieldDTO.setProject(project);
        
        // 이슈 유형
        FieldDTO.Field issueType =  FieldDTO.Field.builder()
                .id(issueTypeCode)
                .build();
        fieldDTO.setIssuetype(issueType);

        // wss 이슈 제목
        fieldDTO.setSummary(projectName);

        // 설명
        String desc = description;
        desc = desc.replace("\t", "@@").replace(" ", "@@");

        Document doc = Jsoup.parse(desc);

        Elements paragraphs = doc.select("p");

        List<FieldDTO.Content> contents = new ArrayList<>();
        for (Element paragraph : paragraphs) {

            // 연속된 공백을 하나의 공백으로 처리
            // String text = tag.text();

            // HTML 엔티티를 일반 텍스트로 변환
            String text = StringEscapeUtils.unescapeHtml4(paragraph.html());

            // @@를 공백 두 칸으로 대체
            text = text.replace("@@", "  ");

            FieldDTO.ContentItem contentItem = FieldDTO.ContentItem.builder().text(text).type("text").build();

            List<FieldDTO.ContentItem> contentItems = new ArrayList<>();
            contentItems.add(contentItem);

            FieldDTO.Content content = FieldDTO.Content.builder().type("paragraph").content(contentItems).build();
            contents.add(content);
        }

        FieldDTO.Description descriptionObject = FieldDTO.Description.builder().version(1).type("doc").content(contents).build();
        fieldDTO.setDescription(descriptionObject);

        createIssueDTO = new CreateIssueDTO<>(fieldDTO);

        String endpoint = "/rest/api/3/issue";
        ResponseIssueDTO responseIssueDTO = null;
        try {
            responseIssueDTO = webClientUtils.post(endpoint, createIssueDTO, ResponseIssueDTO.class).block();

            String key = responseIssueDTO.getKey();

            if(!key.isEmpty() || key != null){
                if(flag.equals("P")){
                    jiraIssue.addMentionAndComment(key,salesManager,"프로젝트 인력 배정 보드에 이슈가 생성되었습니다.");
                }else{
                    jiraIssue.addMentionAndComment(key,salesManager,"유지보수 인력 배정 보드에 이슈가 생성되었습니다.");
                }
                logger.info("[::platformCreateProject::] 인력배정 이슈 생성 성공");
            }

        } catch (Exception e) {
            if (e instanceof WebClientResponseException) {
                WebClientResponseException wcException = (WebClientResponseException) e;
                HttpStatus status = wcException.getStatusCode();
                String body = wcException.getResponseBodyAsString();

                logger.error("[::PlatformProjectImpl::] 인력 배정 이슈 생성시 오류 발생 -> " + status + " : " + body);
            }
        }
    }

    private String getAccountId(String 사용자) {

        String 계정_아이디 = null;

        if (사용자 != null && !사용자.isEmpty()) {
            List<TB_JIRA_USER_Entity> userEntities = TB_JIRA_USER_JpaRepository.findByDisplayNameContaining(사용자);

            if (!userEntities.isEmpty()) {
                계정_아이디 = userEntities.get(0).getAccountId();

            }
        }
        // 담당자 없을 경우 세팅하지 않도록 제거
        /*else {
            TB_JIRA_USER_Entity adminEntity = TB_JIRA_USER_JpaRepository.findByDisplayName("epage div");
            계정_아이디 = adminEntity.getAccountId();
        }*/

        logger.info("[ :: PlatformProjectImpl :: ] getAccountId -> 사용자: " + 사용자 + " - " + "계정 아이디: " + 계정_아이디);

        return 계정_아이디;
    }

}
