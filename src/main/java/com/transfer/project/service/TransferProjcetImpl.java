package com.transfer.project.service;

import com.account.dto.AdminInfoDTO;
import com.account.service.Account;
import com.transfer.issuetype.model.dto.IssueTypeScreenSchemeDTO;
import com.transfer.project.model.dao.TB_PJT_BASE_JpaRepository;
import com.transfer.project.model.dao.TB_JML_JpaRepository;
import com.transfer.project.model.dto.ProjectInfoData;

import com.transfer.project.model.entity.TB_JML_Entity;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;

import com.transfer.project.model.dto.ProjectCreateDTO;
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
public class TransferProjcetImpl implements TransferProjcet{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    

    @Autowired
    private  ProjectConfig projectConfig;

    @Autowired
    private TB_PJT_BASE_JpaRepository TB_PJT_BASE_JpaRepository;

    @Autowired
    private TB_JML_JpaRepository TB_JML_JpaRepository;

    @Autowired
    private Account account;


    @Override
    public ProjectInfoData createProject(ProjectCreateDTO projectCreateDTO) throws Exception{

        try {
        AdminInfoDTO info = account.getAdminInfo(1); //회원 가입 고려시 변경

        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());

        projectCreateDTO.setAssigneeType(projectConfig.assigneType);
        projectCreateDTO.setCategoryId(projectConfig.categoryId);
        projectCreateDTO.setLeadAccountId(projectConfig.leadAccountId);
        projectCreateDTO.setProjectTypeKey(projectConfig.projectTypeKey);


        String endpoint = "/rest/api/3/project";
        ProjectInfoData Response = WebClientUtils.post(webClient, endpoint, projectCreateDTO, ProjectInfoData.class).block();

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
    public Map<String, String> CreateProjectFromDB(int personalId,String projectCode) throws Exception {

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

                // 프로젝트 정보 Setting
                ProjectCreateDTO projectInfo = RequiredData(flag,projectName, projectKey);
                // 프로젝트 생성
                ProjectInfoData Response = createJiraProject(personalId, projectInfo);
                // 프로젝트에 이슈타입 연결
                SetIssueType(Response.getSelf(),flag);
                // 생성 결과 DB 저장
                SaveSuccessData(Response.getKey(),projectCode,projectName,projectInfo.getName());
                // 이관 flag 변경
                CheckMigrateFlag(projectCode);

                result.put("이관 성공",projectCode );

                return result;
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

    public ProjectInfoData createJiraProject(int personalId ,ProjectCreateDTO projectCreateDTO) throws Exception{
        logger.info("JIRA 프로젝트 생성 시작");
        AdminInfoDTO info = account.getAdminInfo(personalId);

        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint = "/rest/api/3/project";
        ProjectInfoData Response = WebClientUtils.post(webClient, endpoint, projectCreateDTO, ProjectInfoData.class).block();
        return Response;

    }

    public ProjectCreateDTO RequiredData(String flag ,String projectName, String projectKey) throws Exception{

        ProjectCreateDTO projectInfo = new ProjectCreateDTO(); // 프로젝트 생성 필수 정보

        projectInfo.setAssigneeType(projectConfig.assigneType);
        projectInfo.setCategoryId(projectConfig.categoryId);
        projectInfo.setKey(projectKey);
        projectInfo.setLeadAccountId(projectConfig.leadAccountId);
        projectInfo.setProjectTypeKey(projectConfig.projectTypeKey);

        String jiraProjectName;

        if (flag.equals("P")) { //프로젝트
            jiraProjectName = "전자문서_프로젝트_WSS_(" + projectName + ")";
            projectInfo.setName(jiraProjectName);

        } else { // 유지보수
            jiraProjectName = "전자문서_유지보수_WSS_(" + projectName + ")";
            projectInfo.setName(jiraProjectName);
        }
        return projectInfo;
    }
    @Transactional
    public String NamingJiraKey() throws Exception{

        long count = TB_JML_JpaRepository.count();
        if (count == 0) {
            return "TWSS1";
        }else{
            String recent_key =TB_JML_JpaRepository.findTopByOrderByMigratedDateDesc().getKey();
            int num = Integer.parseInt(recent_key.substring(4));
            return "TWSS" + (num + 1);
        }
    }
    @Transactional
    public void SaveSuccessData(String key, String projectCode ,String projectName ,String jiraProjectName) throws Exception{

        logger.info("JIRA 프로젝트 생성 결과 저장");
        TB_JML_Entity save_success_data  = new TB_JML_Entity();
        save_success_data.setKey(key);
        save_success_data.setProjectCode(projectCode);
        save_success_data.setWssProjectName(projectName);
        save_success_data.setJiraProjectName(jiraProjectName);
        TB_JML_JpaRepository.save(save_success_data);
    }
    @Transactional
    public void CheckMigrateFlag(String projectCode){
        logger.info("이관 여부 체크");
        TB_PJT_BASE_Entity entity =  TB_PJT_BASE_JpaRepository.findById(projectCode).orElseThrow(() -> new NoSuchElementException("프로젝트 코드 조회에 실패하였습니다.: " + projectCode));
        entity.setMigrateFlag(true);
    }

    public void SetIssueType(String self ,String flag) throws Exception {
        logger.info("프로젝트 이슈타입 연결");
        Integer projectId = Integer.valueOf(self.substring(self.lastIndexOf("/") + 1));

        AdminInfoDTO info = account.getAdminInfo(1);

        IssueTypeScreenSchemeDTO issueTypeScreenSchemeDTO = new IssueTypeScreenSchemeDTO();
        if(flag.equals("P")){
            issueTypeScreenSchemeDTO.setIssueTypeScreenSchemeId(projectConfig.projectIssueType);
        }else{
            issueTypeScreenSchemeDTO.setIssueTypeScreenSchemeId(projectConfig.maintenanceIssueType);
        }

        issueTypeScreenSchemeDTO.setProjectId(projectId);

        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint = "/rest/api/3/issuetypescreenscheme/project";
        WebClientUtils.put(webClient, endpoint, issueTypeScreenSchemeDTO,void.class).block();

    }

}
