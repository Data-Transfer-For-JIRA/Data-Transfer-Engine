package com.api.platform.service;

import com.api.platform.dto.BaseDTO;
import com.api.platform.dto.ReturnMessage;
import com.api.scheduler.backup.model.dao.BACKUP_ISSUE_JpaRepository;
import com.api.scheduler.backup.model.entity.BACKUP_ISSUE_Entity;
import com.config.ProjectConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jira.account.model.dao.TB_JIRA_USER_JpaRepository;
import com.jira.account.model.entity.TB_JIRA_USER_Entity;
import com.jira.account.service.AccountImpl;
import com.jira.issue.model.FieldInfo;
import com.jira.issue.model.FieldInfoCategory;
import com.jira.issue.model.dto.FieldDTO;
import com.jira.issue.model.dto.FieldDTO.User;
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
import com.utils.ConvertHtmlToADF;
import com.utils.SaveLog;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    private BACKUP_ISSUE_JpaRepository BACKUP_ISSUE_JpaRepository;

    @Autowired
    private TB_JLL_JpaRepository TB_JLL_JpaRepository;

    @Autowired
    private  TB_JML_JpaRepository TB_JML_JpaRepository;

    @Autowired
    private ProjectConfig projectConfig;

    @Autowired
    private WebClientUtils webClientUtils;

    private static final String JIRA_PROJECT_PREFIX = "P_";
    private static final String JIRA_MAINTENANCE_PREFIX = "M_";

    /*@Override
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
                            logger.info("[::platformCreateProject::] DB 저장 완료");
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
    }*/

    /*
    *  플랫폼으로 수정하기위해 프로젝트 데이터 조회
    * */
    @Override
    public BaseDTO platformGetProject(String projectFlag, String jiraKey) throws Exception {

        // 지라 프로젝트 정보 조회
        String 기본정보_이슈키;

        String 이슈유형키;

        ProjectDTO 프로젝트 = jiraProject.getJiraProjectInfoByJiraKey(jiraKey); // 지라에서 조회한 프로젝트

        BaseDTO 기본정보_이슈 = new BaseDTO();
        BaseDTO.EssentialDTO.EssentialDTOBuilder 필수데이터빌더 = BaseDTO.EssentialDTO.builder();
        BaseDTO.CommonDTO.CommonDTOBuilder 공통데이터빌더 = BaseDTO.CommonDTO.builder();
        BaseDTO.SelectedDTO.SelectedDTOBuilder 선택데이터빌더 = BaseDTO.SelectedDTO.builder();

        // json 변환
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 기본 정보 이슈 조회
        if (StringUtils.equals(projectFlag, "M")) {
            이슈유형키 = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, "유지보수 기본 정보").getId();

            기본정보_이슈키 = jiraIssue.getBaseIssueKey(jiraKey, 이슈유형키); // 해당 프로젝트의 기본 정보 이슈키 조회
            SearchIssueDTO<SearchMaintenanceInfoDTO> 유지보수_기본정보이슈 = jiraIssue.getMaintenanceIssue(기본정보_이슈키);
            String 설명 = Optional.ofNullable(jiraIssue.이슈_조회(기본정보_이슈키))
                    .map(이슈 -> 이슈.getRenderedFields().getDescription())
                    .map(String::trim)
                    .orElse(null);

            String jsonRequestBody = objectMapper.writeValueAsString(유지보수_기본정보이슈);
            logger.info("json: " + jsonRequestBody);
            SearchMaintenanceInfoDTO 유지보수_기본정보 = 유지보수_기본정보이슈.getFields();
            logger.info("유지보수 기본 정보: " + 유지보수_기본정보);

            // 프로젝트 정보 매핑

            // 필수 데이터
            setBuilder(projectFlag, 필수데이터빌더::projectFlag);
            setBuilder(유지보수_기본정보.getMaintenanceName(), 필수데이터빌더::projectName);

            // 공통 데이터
            setBuilder(유지보수_기본정보.getMaintenanceCode(), 공통데이터빌더::projectCode);
            setBuilder(사용자_추출(유지보수_기본정보.getAssignee()), 공통데이터빌더::assignee);
            setBuilder(사용자_추출(유지보수_기본정보.getSubAssignee()), 공통데이터빌더::subAssignee);
            setBuilder(사용자_추출(유지보수_기본정보.getSalesManager()), 공통데이터빌더::salesManager);
            setBuilder(유지보수_기본정보.getContractor(), 공통데이터빌더::contractor);
            setBuilder(유지보수_기본정보.getClient(), 공통데이터빌더::client);

            setBuilder(
                    () -> 유지보수_기본정보.getBarcodeType(),
                    바코드타입 -> 바코드타입.getValue(),
                    공통데이터빌더::barcodeType
            );

            setBuilder(
                    () -> 유지보수_기본정보.getPrinterSupportRange(),
                    프린터지원 -> 프린터지원.getValue(),
                    공통데이터빌더::printerSupportRange
            );

            setBuilder(설명, 공통데이터빌더::description);

            setBuilder(
                    유지보수_기본정보::getProductInfo1,
                    기본정보 -> {
                        List<String> 제품정보 = new ArrayList<>();
                        for (FieldDTO.Field 제품 : 기본정보) {
                            제품정보.add(제품.getValue());
                        }
                        return 제품정보;
                    },
                    공통데이터빌더::productInfo1
            );

            setBuilder(
                    유지보수_기본정보::getProductInfo2,
                    기본정보 -> {
                        List<String> 제품정보 = new ArrayList<>();
                        for (FieldDTO.Field 제품 : 기본정보) {
                            제품정보.add(제품.getValue());
                        }
                        return 제품정보;
                    },
                    공통데이터빌더::productInfo2
            );

            setBuilder(
                    유지보수_기본정보::getProductInfo3,
                    기본정보 -> {
                        List<String> 제품정보 = new ArrayList<>();
                        for (FieldDTO.Field 제품 : 기본정보) {
                            제품정보.add(제품.getValue());
                        }
                        return 제품정보;
                    },
                    공통데이터빌더::productInfo3
            );

            setBuilder(
                    유지보수_기본정보::getProductInfo4,
                    기본정보 -> {
                        List<String> 제품정보 = new ArrayList<>();
                        for (FieldDTO.Field 제품 : 기본정보) {
                            제품정보.add(제품.getValue());
                        }
                        return 제품정보;
                    },
                    공통데이터빌더::productInfo4
            );

            setBuilder(
                    유지보수_기본정보::getProductInfo5,
                    기본정보 -> {
                        List<String> 제품정보 = new ArrayList<>();
                        for (FieldDTO.Field 제품 : 기본정보) {
                            제품정보.add(제품.getValue());
                        }
                        return 제품정보;
                    },
                    공통데이터빌더::productInfo5
            );

            // 선택 데이터
            setBuilder(
                    유지보수_기본정보::getContractStatus,
                    FieldDTO.Field::getValue,
                    선택데이터빌더::contractStatus
            );

            setBuilder(유지보수_기본정보::getMaintenanceStartDate, 선택데이터빌더::maintenanceStartDate);
            setBuilder(유지보수_기본정보::getMaintenanceEndDate, 선택데이터빌더::maintenanceEndDate);

            setBuilder(
                    유지보수_기본정보::getInspectionMethod,
                    FieldDTO.Field::getValue,
                    선택데이터빌더::inspectionMethod
            );

            setBuilder(유지보수_기본정보::getInspectionMethodEtc, 선택데이터빌더::inspectionMethodEtc);

            setBuilder(
                    유지보수_기본정보::getInspectionCycle,
                    FieldDTO.Field::getValue,
                    선택데이터빌더::inspectionCycle
            );

        } else {
            이슈유형키 = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, "프로젝트 기본 정보").getId();

            기본정보_이슈키 = jiraIssue.getBaseIssueKey(jiraKey, 이슈유형키);
            SearchIssueDTO<SearchProjectInfoDTO> 프로젝트_기본정보이슈 = jiraIssue.getProjectIssue(기본정보_이슈키);
            String 설명 = Optional.ofNullable(jiraIssue.이슈_조회(기본정보_이슈키))
                    .map(이슈 -> 이슈.getRenderedFields().getDescription())
                    .map(String::trim)
                    .orElse(null);

            SearchProjectInfoDTO 프로젝트_기본정보 = 프로젝트_기본정보이슈.getFields();
            logger.info("프로젝트 기본 정보: " + 프로젝트_기본정보);

            // 프로젝트 정보 매핑

            // 필수 데이터
            setBuilder(projectFlag, 필수데이터빌더::projectFlag);
            setBuilder(프로젝트_기본정보.getProjectName(), 필수데이터빌더::projectName);

            // 공통 데이터
            setBuilder(프로젝트_기본정보.getProjectCode(), 공통데이터빌더::projectCode);
            setBuilder(사용자_추출(프로젝트_기본정보.getAssignee()), 공통데이터빌더::assignee);
            setBuilder(사용자_추출(프로젝트_기본정보.getSubAssignee()), 공통데이터빌더::subAssignee);
            setBuilder(사용자_추출(프로젝트_기본정보.getSalesManager()), 공통데이터빌더::salesManager);
            setBuilder(프로젝트_기본정보.getContractor(), 공통데이터빌더::contractor);
            setBuilder(프로젝트_기본정보.getClient(), 공통데이터빌더::client);

            setBuilder(
                    () -> 프로젝트_기본정보.getBarcodeType(),
                    바코드타입 -> 바코드타입.getValue(),
                    공통데이터빌더::barcodeType
            );

            setBuilder(
                    () -> 프로젝트_기본정보.getPrinterSupportRange(),
                    프린터지원 -> 프린터지원.getValue(),
                    공통데이터빌더::printerSupportRange
            );

            setBuilder(설명, 공통데이터빌더::description);

            setBuilder(
                    프로젝트_기본정보::getProductInfo1,
                    기본정보 -> {
                        List<String> 제품정보 = new ArrayList<>();
                        for (FieldDTO.Field 제품 : 기본정보) {
                            제품정보.add(제품.getValue());
                        }
                        return 제품정보;
                    },
                    공통데이터빌더::productInfo1
            );

            setBuilder(
                    프로젝트_기본정보::getProductInfo2,
                    기본정보 -> {
                        List<String> 제품정보 = new ArrayList<>();
                        for (FieldDTO.Field 제품 : 기본정보) {
                            제품정보.add(제품.getValue());
                        }
                        return 제품정보;
                    },
                    공통데이터빌더::productInfo2
            );

            setBuilder(
                    프로젝트_기본정보::getProductInfo3,
                    기본정보 -> {
                        List<String> 제품정보 = new ArrayList<>();
                        for (FieldDTO.Field 제품 : 기본정보) {
                            제품정보.add(제품.getValue());
                        }
                        return 제품정보;
                    },
                    공통데이터빌더::productInfo3
            );

            setBuilder(
                    프로젝트_기본정보::getProductInfo4,
                    기본정보 -> {
                        List<String> 제품정보 = new ArrayList<>();
                        for (FieldDTO.Field 제품 : 기본정보) {
                            제품정보.add(제품.getValue());
                        }
                        return 제품정보;
                    },
                    공통데이터빌더::productInfo4
            );

            setBuilder(
                    프로젝트_기본정보::getProductInfo5,
                    기본정보 -> {
                        List<String> 제품정보 = new ArrayList<>();
                        for (FieldDTO.Field 제품 : 기본정보) {
                            제품정보.add(제품.getValue());
                        }
                        return 제품정보;
                    },
                    공통데이터빌더::productInfo5
            );

            // 선택 데이터
            setBuilder(프로젝트_기본정보::getProjectAssignmentDate, 선택데이터빌더::projectAssignmentDate);

            setBuilder(
                    프로젝트_기본정보::getProjectProgressStep,
                    FieldDTO.Field::getValue,
                    선택데이터빌더::projectProgressStep
            );
        }

        기본정보_이슈.setEssential(필수데이터빌더.build());
        기본정보_이슈.setCommon(공통데이터빌더.build());
        기본정보_이슈.setSelected(선택데이터빌더.build());

        return 기본정보_이슈;
    }

    @Override
    public BaseDTO platformGetIssue(String jiraIssueKey) throws Exception {

        BaseDTO 이슈 = new BaseDTO();
        BaseDTO.EssentialDTO.EssentialDTOBuilder 필수데이터빌더 = BaseDTO.EssentialDTO.builder();
        BaseDTO.CommonDTO.CommonDTOBuilder 공통데이터빌더 = BaseDTO.CommonDTO.builder();
        BaseDTO.SelectedDTO.SelectedDTOBuilder 선택데이터빌더 = BaseDTO.SelectedDTO.builder();

        // 이슈 조회
        SearchIssueDTO<SearchMaintenanceInfoDTO> 유지보수_기본정보이슈 = jiraIssue.getMaintenanceIssue(jiraIssueKey);
        String 설명 = Optional.ofNullable(jiraIssue.이슈_조회(jiraIssueKey))
                .map(issue -> issue.getRenderedFields().getDescription())
                .map(String::trim)
                .orElse(StringUtils.EMPTY);

        setBuilder(설명, 공통데이터빌더::description);

        이슈.setEssential(필수데이터빌더.build());
        이슈.setCommon(공통데이터빌더.build());
        이슈.setSelected(선택데이터빌더.build());

        return 이슈;
    }

    @Override
    public Map<String, String> platformCreateProject(String jiraProjectCode, String projectFlag, String projectName, String projectCode, String assignees, String salesManager) throws Exception {

        Map<String, String> result = new HashMap<>();
        CreateProjectDTO createProjectDTO = new CreateProjectDTO();
        String jiraProjectName = "";

        if (projectFlag.equals("P")) { // 프로젝트
            jiraProjectName = JIRA_PROJECT_PREFIX + projectName;
            createProjectDTO.setName(jiraProjectName);
            createProjectDTO.setExistingProjectId(projectConfig.projectTemplate);
        } else { // 유지보수
            jiraProjectName = JIRA_MAINTENANCE_PREFIX + projectName;
            createProjectDTO.setName(jiraProjectName);
            createProjectDTO.setExistingProjectId(projectConfig.maintenanceTemplate);
        }
        createProjectDTO.setKey(jiraProjectCode);

        // 템플릿으로 프로젝트 생성
        String endpoint = "/rest/simplified/latest/project/shared";
        String finalJiraProjectName = jiraProjectName;

        result.put("jiraProjectCode", jiraProjectCode);
        result.put("jiraProjectName", finalJiraProjectName);

        if (jiraProject.checkJiraProjectName(finalJiraProjectName)) {
            logger.error("[ :: PlatformProjectImpl :: ] 프로젝트명 중복");
            result.put("result", "DUPLICATE");
            return result;
        }

        try {

            logger.info("[::platformCreateProject::] dto 확인 " + createProjectDTO.toString());
            CreateProjectResponseDTO response = webClientUtils.post(endpoint, createProjectDTO, CreateProjectResponseDTO.class)
                    .doOnSuccess(res -> {
                        // 성공적으로 응답을 받았을 때
                        result.put("result", "프로젝트 생성 성공");

                        try {
                            jiraProject.saveSuccessData(jiraProjectCode, res.getProjectId(), projectCode, projectName, finalJiraProjectName, projectFlag, assignees, salesManager); // DB 저장
                            logger.info("[::platformCreateProject::] DB 저장 완료");
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

        ObjectMapper objectMapper = new ObjectMapper();
        String 입력_데이터 = objectMapper.writeValueAsString(baseDTO);
        logger.info("[ :: PlatformProjectImpl :: platformService :: ] 입력 데이터: " + 입력_데이터);

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
        Map<String, String> createInfo = platformCreateProject(jiraProjectCode, projectFlag, projectName, projectCode, assignees, salesManager);

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
    public Map<String, String> upDateProjectInfo(String jiraKey, BaseDTO baseDTO) throws Exception {

        Map<String, String> result;

        // 기존 프로젝트 정보
        ProjectDTO 기존_프로젝트_정보 = jiraProject.getJiraProjectInfoByJiraKey(jiraKey);
        String 기존_프로젝트_이름 = 기존_프로젝트_정보.getName();
        String 기존_담당자_이름 = account.getUserNameByJiraAccountId(기존_프로젝트_정보.getLead().getAccountId());

        // 프로젝트 정보 업데이트
        CreateProjectDTO 업데이트_정보 = new CreateProjectDTO();

        // 프로젝트 이름
        String 프로젝트_이름 = baseDTO.getEssential() != null ? baseDTO.getEssential().getProjectName() : null;

        if (프로젝트_이름 != null) {
            if ("P".equals(baseDTO.getEssential().getProjectFlag())) {
                프로젝트_이름 = JIRA_PROJECT_PREFIX + 프로젝트_이름;
            } else {
                프로젝트_이름 = JIRA_MAINTENANCE_PREFIX + 프로젝트_이름;
            }
            if (!프로젝트_이름.isEmpty() && !프로젝트_이름.equals(기존_프로젝트_이름)) {
                업데이트_정보.setName(프로젝트_이름);
            }
        }

        // 담당자
        String 담당자_이름 = baseDTO.getCommon().getAssignee();
        if (담당자_이름 != null && !담당자_이름.isEmpty() && !담당자_이름.equals(기존_담당자_이름)) {
            setBuilder(
                    () -> getAccountId(담당자_이름),
                    업데이트_정보::setLeadAccountId
            );
        }
        업데이트_정보.setKey(jiraKey);

        result = jiraProject.updateProjectInfo(업데이트_정보);
        String projectStatus = result.get("projectResult");

        // 기본 정보 이슈 업데이트
        String issueKey = jiraIssue.getBaseIssueKeyByJiraKey(jiraKey);

        if ("UPDATE_DUPLICATE".equals(projectStatus)) {
            result.put("jiraIssueKey", issueKey);
            result.put("issueResult", "UPDATE_FAIL");
        } else if (issueKey != null) {
            Map<String, String> issueResult = updateBaseIssue(issueKey, baseDTO);
            result.putAll(issueResult);
        }

        return result;
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
        result.put("jiraIssueKey", issueKey);
        if (issueKey != null) {
            String endpoint = "/rest/api/3/issue/" + issueKey;
            Optional<Boolean> response = webClientUtils.executePut(endpoint, updateIssueDTO);
            if (response.isPresent()) {
                if (response.get()) {
                    result.put("issueResult", "UPDATE_SUCCESS");

                    return result;
                }
            }
        }

        result.put("issueResult", "UPDATE_FAIL");

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
            JsonNode ADF_변환 = ConvertHtmlToADF.converter(설명);
            customBuilder.description(ADF_변환);
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
        String 설명 = description;
        if (!StringUtils.isEmpty(설명)) {
            JsonNode ADF_변환 = ConvertHtmlToADF.converter(설명);
            fieldDTO.setDescription(ADF_변환);
        }

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

    private String 사용자_추출(User 사용자) {

        return Optional.ofNullable(사용자)
                .map(이름 -> 이름.getDisplayName())
                .map(account::이름_추출)
                .orElse(null);
    }

    @Override
    public Map<String, String> createTicket(String summary, String description) throws Exception {

        Map<String, String> createInfo = new HashMap<>();

        MaintenanceInfoDTO maintenanceInfoDTO = new MaintenanceInfoDTO();
        maintenanceInfoDTO.setProject(new FieldDTO.Project("TED779","13796"));
        maintenanceInfoDTO.setIssuetype(new FieldDTO.Field("10002"));

        if (!StringUtils.isEmpty(summary)) {
            maintenanceInfoDTO.setSummary(summary);
        }
        if (!StringUtils.isEmpty(description)) {
            JsonNode ADF_변환 = ConvertHtmlToADF.converter(description);
            maintenanceInfoDTO.setDescription(ADF_변환);
        }

        CreateIssueDTO createIssueDTO = new CreateIssueDTO(maintenanceInfoDTO);

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
            createInfo.put("result", "이슈 생성 성공");
        } else {
            createInfo.put("result", "이슈 생성 실패");
        }

        return createInfo;
    }

    @Override
    public Optional<BACKUP_ISSUE_Entity> 티켓_정보_조회(String 지라_이슈_키){
        return BACKUP_ISSUE_JpaRepository.findById(지라_이슈_키);
    }

    @Override
    public Page<BACKUP_ISSUE_Entity> 프로젝트에_생성된_티켓_정보_조회(String jiraProjectKey , int page, int size){

        Pageable pageable = PageRequest.of(page,size);

        return BACKUP_ISSUE_JpaRepository.findByJiraProjectKeyOrderByCreateDate(jiraProjectKey,pageable);
    }

}
