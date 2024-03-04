package com.api.platform.service;

import com.jira.account.dao.TB_JIRA_USER_JpaRepository;
import com.jira.account.entity.TB_JIRA_USER_Entity;
import com.api.platform.dto.BaseDTO;
import com.api.platform.dto.ReturnMessage;
import com.jira.issue.model.dto.*;
import com.jira.issue.model.FieldInfo;
import com.jira.issue.model.FieldInfoCategory;
import com.jira.issue.model.dto.weblink.RequestWeblinkDTO;
import com.jira.issue.service.JiraIssueImpl;
import com.jira.project.model.dao.TB_JLL_JpaRepository;
import com.jira.project.model.dao.TB_JML_JpaRepository;
import com.jira.project.model.dto.CreateProjectDTO;
import com.jira.project.model.dto.CreateProjectResponseDTO;
import com.jira.project.model.entity.TB_JLL_Entity;
import com.jira.project.model.entity.TB_JML_Entity;
import com.jira.project.service.JiraProjectImpl;
import com.utils.ProjectConfig;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
        String projectName = essentialDTO.getProjectName();
        String projectCode = commonDTO.getProjectCode();
        String assignee = commonDTO.getAssignee();
        String subAssignee = commonDTO.getSubAssignee();
        String description = commonDTO.getDescription();
        // 담당자 설정
        String assignees = setAssignees(assignee, subAssignee);
        // 프로젝트 생성
        Map<String, String> createInfo = platformCreateProject(jiraProjectCode, projectFlag, projectName, projectCode, assignees);

        String createProjectFlag = createInfo.get("result");
        String jiraProjectName = createInfo.get("jiraProjectName");

        if (createProjectFlag.equals("프로젝트 생성 성공")) {

            CreateIssueDTO<?> createIssueDTO;
            if (projectFlag.equals("P")) { // 프로젝트 타입

                if(commonDTO.getAllocationFlag() == true){
                    createStaffAllocationIssue(description,projectName,projectFlag);
                }

                ProjectInfoDTO.ProjectInfoDTOBuilder<?, ?> projectBuilder = ProjectInfoDTO.builder();
                projectBuilder = setCommonFields(projectBuilder, jiraProjectCode, commonDTO);

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

                if(commonDTO.getAllocationFlag() == true){
                    createStaffAllocationIssue(description,projectName,projectFlag);
                }

                MaintenanceInfoDTO.MaintenanceInfoDTOBuilder<?, ?> maintenanceBuilder = MaintenanceInfoDTO.builder();
                maintenanceBuilder = setCommonFields(maintenanceBuilder, jiraProjectCode, commonDTO );

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
        customBuilder.project(new FieldDTO.Project(jiraProjectCode, null));

        // 제목
        String summary = "기본 정보";
        customBuilder.summary(summary);

        // 담당자 및 부 담당자
        String assignees = setAssignees(commonDTO.getAssignee(), commonDTO.getSubAssignee());
        List<String> assigneeList = jiraIssue.getSeveralAssigneeId(assignees);

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
            if (jiraIssue.getSeveralAssigneeId(commonDTO.getSalesManager()) != null && !jiraIssue.getSeveralAssigneeId(commonDTO.getSalesManager()).isEmpty()) {
                String salesManagerId = jiraIssue.getSeveralAssigneeId(commonDTO.getSalesManager()).get(0);
                if (salesManagerId != null) {
                    customBuilder.salesManager(new FieldDTO.User(salesManagerId));
                }
            }
        }

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
        if (assigneeList != null && !assigneeList.isEmpty()) {
            TB_JIRA_USER_Entity userEntity = TB_JIRA_USER_JpaRepository.findByAccountId(assigneeList.get(0));

            if (userEntity != null) {
                setBuilder(
                        () -> FieldInfo.ofLabel(FieldInfoCategory.TEAM, userEntity.getTeam()),
                        fieldInfo -> fieldInfo.getId(),
                        customBuilder::team
                );

                setBuilder(
                        () -> FieldInfo.ofLabel(FieldInfoCategory.PART, userEntity.getPart()),
                        fieldInfo -> new FieldDTO.Field(fieldInfo.getId()),
                        customBuilder::part
                );
            }
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
        if (!commonDTO.getDescription().trim().isEmpty()) {

            String desc = commonDTO.getDescription();
            desc = desc.replace("\t", "@@").replace(" ", "@@");

            Document doc = Jsoup.parse(desc);

            Elements paragraphs  = doc.select("p");

            List<FieldDTO.Content> contents = new ArrayList<>();
            for (Element paragraph : paragraphs ) {

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

            FieldDTO.Description description = FieldDTO.Description.builder().version(1).type("doc").content(contents).build();
            customBuilder.description(description);
        }

        // 기타 정보
        if (!commonDTO.getEtc().trim().isEmpty()) {
            customBuilder.etc(jiraIssue.setDescription(Collections.singletonList(commonDTO.getEtc())));
        }

        return customBuilder;
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

    private void createStaffAllocationIssue(String description, String projectName, String flag)throws Exception{

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
                logger.info("[::platformCreateProject::] 인력배정 이슈 생성 성공");
            }

        } catch (Exception e) {
            if (e instanceof WebClientResponseException) {
                WebClientResponseException wcException = (WebClientResponseException) e;
                HttpStatus status = wcException.getStatusCode();
                String body = wcException.getResponseBodyAsString();

                System.out.println("[::PlatformProjectImpl::] 인력 배정 이슈 생성시 오류 발생 -> " + status + " : " + body);
            }
        }
    }

}
