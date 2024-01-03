package com.transfer.project.service;

import com.account.dto.AdminInfoDTO;
import com.account.service.Account;
import com.fasterxml.jackson.databind.JsonNode;
import com.transfer.issue.service.TransferIssue;
import com.transfer.issuetype.model.dto.IssueTypeSchemeDTO;
import com.transfer.issuetype.model.dto.IssueTypeScreenScheme;
import com.transfer.project.model.dao.TB_PJT_BASE_JpaRepository;
import com.transfer.project.model.dao.TB_JML_JpaRepository;
import com.transfer.project.model.dto.CreateProjectResponseDTO;

import com.transfer.project.model.dto.ProjectDTO;
import com.transfer.project.model.entity.TB_JML_Entity;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;

import com.transfer.project.model.dto.CreateProjectDTO;
import com.utils.ProjectConfig;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@AllArgsConstructor
@Service("transferProjcet")
public class TransferProjectImpl implements TransferProject {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    

    @Autowired
    private  ProjectConfig projectConfig;

    @Autowired
    private TB_PJT_BASE_JpaRepository TB_PJT_BASE_JpaRepository;

    @Autowired
    private TB_JML_JpaRepository TB_JML_JpaRepository;

    @Autowired
    private Account account;

    @Autowired
    private TransferIssue transferIssue;


    @Override
    public CreateProjectResponseDTO createProject(CreateProjectDTO createProjectDTO) throws Exception{

        try {
        AdminInfoDTO info = account.getAdminInfo(1); //회원 가입 고려시 변경

        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());

//        createProjectDTO.setAssigneeType(projectConfig.assigneType);
//        createProjectDTO.setCategoryId(projectConfig.categoryId);
//        createProjectDTO.setLeadAccountId(projectConfig.leadAccountId);
//        createProjectDTO.setProjectTypeKey(projectConfig.projectTypeKey);


        String endpoint = "/rest/api/3/project";
        CreateProjectResponseDTO Response = WebClientUtils.post(webClient, endpoint, createProjectDTO, CreateProjectResponseDTO.class).block();

            return Response;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }



    @Override
    @Transactional
    public Page<TB_PJT_BASE_Entity> getDataBaseProjectData(int pageIndex, int pageSize) throws Exception{

        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        Page<TB_PJT_BASE_Entity> page = TB_PJT_BASE_JpaRepository.findAllByOrderByCreatedDateDesc(pageable);

        return page;
    }

    @Override
    @Transactional
    public Page<TB_PJT_BASE_Entity> getDataBeforeProjectData(int pageIndex, int pageSize) throws Exception{

        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        Page<TB_PJT_BASE_Entity> page = TB_PJT_BASE_JpaRepository.findAllByMigrateFlagFalseOrderByCreatedDateDesc(pageable);

        return page;
    }

    @Override
    @Transactional
    public Page<TB_PJT_BASE_Entity> getDataBeforeSeachProjectData(String seachKeyWord,int pageIndex, int pageSize) throws Exception{

        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<TB_PJT_BASE_Entity> searchResult = TB_PJT_BASE_JpaRepository.findByProjectNameContainingAndMigrateFlagFalseOrderByCreatedDateDesc(seachKeyWord,pageable);

        return searchResult;
    }


    @Override
    @Transactional
    public Page<TB_JML_Entity> getDataAfterProjectData(int pageIndex, int pageSize) throws Exception{

        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<TB_JML_Entity> page = TB_JML_JpaRepository.findAllByOrderByMigratedDateDesc(pageable);


        return page;
    }

    @Override
    @Transactional
    public Page<TB_JML_Entity> getDataAfterSeachProjectData(String seachKeyWord,int pageIndex, int pageSize) throws Exception{
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<TB_JML_Entity> searchResult = TB_JML_JpaRepository.findByWssProjectNameContainingOrderByMigratedDateDesc(seachKeyWord,pageable);

        return searchResult;
    }

    @Override
    @Transactional
    public Page<TB_PJT_BASE_Entity>  getTransferredProjectsList(int pageIndex, int pageSize) throws Exception{
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<TB_PJT_BASE_Entity> searchResult = TB_PJT_BASE_JpaRepository.findAllByMigrateFlagTrueAndIssueMigrateFlagTrueOrderByCreatedDateDesc(pageable);
        return searchResult;
    }


    @Override
    @Transactional
    public Map<String, String> CreateProjectFromDB(int personalId,String projectCode) throws Exception{
        logger.info("프로젝트 생성 시작");

        Map<String, String> result = new HashMap<>();
        try {
            // 프로젝트 조회
            Optional<TB_PJT_BASE_Entity> table_info = TB_PJT_BASE_JpaRepository.findById(projectCode);
            if (table_info.isPresent()) { // 프로젝트 조회 성공
                logger.info("WSS 프로젝트 조회");
                String flag = table_info.get().getProjectFlag();
                String projectName = table_info.get().getProjectName();
                String projectKey = NamingJiraKey();
                boolean migrateFlag = table_info.get().getMigrateFlag();
                String assignees = table_info.get().getAssignedEngineer();
                if (!migrateFlag) {
                    // 프로젝트 정보 Setting
                    CreateProjectDTO projectInfo = RequiredData(flag,projectName, projectKey);
                    // 프로젝트 생성
                    CreateProjectResponseDTO Response = createJiraProject(personalId, projectInfo);
                    // 프로젝트에 이슈타입 연결
                    // SetIssueType(Response.getSelf(),flag); // 프로젝트 기본 생성 방법
                    // 생성 결과 DB 저장
                    //SaveSuccessData(Response.getKey(),projectCode,projectName,projectInfo.getName(),flag); // 프로젝트 기본 생성 방법
                    saveSuccessData(Response.getProjectKey() , Response.getProjectId(),projectCode,projectName,projectInfo.getName(),flag,assignees); // 템플릿을 통한 생성 방법
                    // 디비 이관 flag 변경
                    CheckMigrateFlag(projectCode);

                    result.put("이관 성공",projectCode );
                    return result;
                }else{
                    result.put("이미 이관한 프로젝트",projectCode );
                    return result;
                }

            } else { //프로젝트 조회 실패

                result.put("프로젝트 조회 실패", projectCode);
                return result;
            }
        }catch (Exception e){ // 프로젝트 이관 실패 시

            result.put("이관 실패", projectCode);
            logger.error(e.getMessage());

        }

        return result;
    }

    public CreateProjectResponseDTO createJiraProject(int personalId , CreateProjectDTO createProjectDTO) throws Exception{
        logger.info("JIRA 프로젝트 생성 시작");
        AdminInfoDTO info = account.getAdminInfo(personalId);

        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        // 프로젝트 기본 생성 방법
        // String endpoint = "/rest/api/3/project";

        // 템플릿을 통한 생성 방법
        String endpoint = "/rest/simplified/latest/project/shared";

        CreateProjectResponseDTO Response = WebClientUtils.post(webClient, endpoint, createProjectDTO, CreateProjectResponseDTO.class).block();
        return Response;

    }
    // 프로젝트 기본 생성 방법
    public CreateProjectDTO RequiredData(String flag , String projectName, String projectKey) throws Exception{
        logger.info("JIRA 프로젝트 생성시 필요 데이터 조합");
        CreateProjectDTO projectInfo = new CreateProjectDTO(); // 프로젝트 생성 필수 정보

        /*
        // 프로젝트 기본 생성 방법
        projectInfo.setAssigneeType(projectConfig.assigneType);
        projectInfo.setCategoryId(projectConfig.categoryId);
        projectInfo.setKey(projectKey);
        projectInfo.setLeadAccountId(projectConfig.leadAccountId);
        projectInfo.setProjectTypeKey(projectConfig.projectTypeKey);*/

        // 템플릿을 통한 생성 방법
        projectInfo.setKey(projectKey);

        String jiraProjectName;

        if (flag.equals("P")) { //프로젝트
            jiraProjectName = "ED-P_WSS_" + projectName;
            projectInfo.setName(jiraProjectName);
            projectInfo.setExistingProjectId(projectConfig.projectTemplate);

        } else { // 유지보수
            jiraProjectName = "ED-M_WSS_" + projectName;
            projectInfo.setName(jiraProjectName);
            projectInfo.setExistingProjectId(projectConfig.maintenanceTemplate);
        }
        return projectInfo;
    }


    /*
     * 리펙토링 필요 2023 12 02
     * - 중복 제거 하였지만 효율서이 떨어짐
     * */
    public String NamingJiraKey() throws Exception {
        logger.info("JIRA 프로젝트 키 생성");
        String jiraKey;
        long count = TB_JML_JpaRepository.count();
        if (count == 0) { // 최초
            return "ED1";
        } else {
            String recentKey = TB_JML_JpaRepository.findTopByOrderByMigratedDateDesc().getKey();
            int num = Integer.parseInt(recentKey.substring(2));
            while (true) {
                num++;
                jiraKey = "ED" + num;
                if (checkValidationJiraKey(jiraKey)) {
                    return jiraKey;
                }
            }
        }
    }
    /*
    *  리펙토링 필요 2023 12 02
    * */
    public Boolean checkValidationJiraKey(String key) throws Exception{
        logger.info("JIRA 프로젝트 키 유효성 체크 ");
        try {
            AdminInfoDTO info = account.getAdminInfo(1);
            WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
            String endpoint = "/rest/api/3/project/"+key;
            WebClientUtils.get(webClient,endpoint, JsonNode.class).block();
            return false; // 있는 프로젝트
        }catch (Exception e){
            return true; // 없는 프로젝트
        }
    }

    public void saveSuccessData(String key, String id , String projectCode , String projectName , String jiraProjectName, String flag , String projectAssignees) throws Exception{

        logger.info("JIRA 프로젝트 생성 결과 저장");
        TB_JML_Entity save_success_data  = new TB_JML_Entity();
        save_success_data.setKey(key);
        save_success_data.setId(id);
        save_success_data.setProjectCode(projectCode);
        save_success_data.setWssProjectName(projectName);
        save_success_data.setJiraProjectName(jiraProjectName);
        save_success_data.setFlag(flag);
        save_success_data.setProjectAssignees(projectAssignees);
        TB_JML_JpaRepository.save(save_success_data);
    }
    public void CheckMigrateFlag(String projectCode){
        logger.info("이관 여부 체크");
        TB_PJT_BASE_Entity entity =  TB_PJT_BASE_JpaRepository.findById(projectCode).orElseThrow(() -> new NoSuchElementException("프로젝트 코드 조회에 실패하였습니다.: " + projectCode));
        entity.setMigrateFlag(true);
    }

    public void SetIssueType(String self ,String flag) throws Exception {
        logger.info("프로젝트 이슈타입 연결");
        Integer projectId = Integer.valueOf(self.substring(self.lastIndexOf("/") + 1));

        AdminInfoDTO info = account.getAdminInfo(1);

        IssueTypeSchemeDTO issueTypeSchemeDTO = new IssueTypeSchemeDTO();
        if(flag.equals("P")){
            issueTypeSchemeDTO.setIssueTypeSchemeId(projectConfig.projectIssueTypeScheme);
        }else{
            issueTypeSchemeDTO.setIssueTypeSchemeId(projectConfig.maintenanceIssueTypeScheme);
        }

        issueTypeSchemeDTO.setProjectId(projectId);

        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint = "/rest/api/2/issuetypescheme/project";
        WebClientUtils.put(webClient, endpoint, issueTypeSchemeDTO,void.class).block();


        setIssueTypeScreen(flag, String.valueOf(projectId));

    }

    public void setIssueTypeScreen( String flag ,String projectId) throws Exception{
        logger.info("이슈 타입 스크린 프로젝트 연결");
        IssueTypeScreenScheme issueTypeScreenScheme = new IssueTypeScreenScheme();


        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint;
        if(flag.equals("P")){
            issueTypeScreenScheme.setIssueTypeScreenSchemeId(projectConfig.projectIssueTypeScreenScheme);
            endpoint = "/rest/api/2/issuetypescreenscheme/project";
        }else{
            issueTypeScreenScheme.setIssueTypeScreenSchemeId(projectConfig.maintenanceIssueTypeScreenScheme);
            endpoint = "/rest/api/2/issuetypescreenscheme/project";
        }
        issueTypeScreenScheme.setProjectId(projectId);

        WebClientUtils.put(webClient, endpoint, issueTypeScreenScheme,void.class).block();
    }


    @Override
    public ProjectDTO reassignProjectLeader(String jiraProjectCode, String assignee) throws Exception{
        logger.info("[::reassignProjectLeader::]  프로젝트 담당자 지정 변경");
        try {
            AdminInfoDTO info = account.getAdminInfo(1);
            WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
            String endpoint ="/rest/api/3/project/"+jiraProjectCode;

            CreateProjectDTO createProjectDTO = new CreateProjectDTO();
            createProjectDTO.setAssigneeType("PROJECT_LEAD");
            String assigneeId = null;
            if(Character.isDigit(assignee.charAt(0))) { // 숫자로 시작하면
                assigneeId = assignee;
            } else if(Character.isLetter(assignee.charAt(0))) {// 문자로 시작하면
                assigneeId =  transferIssue.getOneAssigneeId(assignee);
            }
            createProjectDTO.setLeadAccountId(assigneeId);

            ProjectDTO returnDate = WebClientUtils.put(webClient, endpoint, createProjectDTO, ProjectDTO.class).block();

            return returnDate;
        }catch (Exception e){
            return null;
        }
    }

}
