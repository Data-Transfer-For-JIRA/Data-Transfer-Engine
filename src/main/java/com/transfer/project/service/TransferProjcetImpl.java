package com.transfer.project.service;

import com.transfer.project.dao.TB_PJT_BASE_JpaRepository;
import com.transfer.project.model.ProjectInfoData;
import com.transfer.project.model.TB_PJT_BASE_Entity;
import com.utils.HashString;
import com.utils.JiraConfig;
import com.transfer.project.model.ProjectCreateDTO;
import com.utils.ProjectConfig;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service("transferProjcet")
public class TransferProjcetImpl implements TransferProjcet{

    @Autowired
    private JiraConfig jiraConfig;

    @Autowired
    private  ProjectConfig projectConfig;

    @Autowired
    private TB_PJT_BASE_JpaRepository TB_PJT_BASE_JpaRepository;

    @Override
    public ProjectInfoData createProject(ProjectCreateDTO projectCreateDTO) throws Exception{

        WebClient webClient = WebClientUtils.createJiraWebClient(jiraConfig.baseUrl, jiraConfig.jiraID, jiraConfig.apiToken);
        String endpoint = "/rest/api/3/project";
        ProjectInfoData Response = WebClientUtils.post(webClient, endpoint, projectCreateDTO, ProjectInfoData.class).block();
        return Response;

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
    public Map<String, String> CreateProjectFromDB(String projectCode) throws Exception{

        TB_PJT_BASE_Entity  table_info = TB_PJT_BASE_JpaRepository.findByProjectCode(projectCode);

        String flag  = table_info.getBS_PJTFLAG();

        String projectName = table_info.getBS_PJTNAME();

        String projectKey = HashString.hashing(projectCode);

        ProjectCreateDTO projectInfo = new ProjectCreateDTO();

        projectInfo.setAssigneeType(projectConfig.assigneType);
        projectInfo.setCategoryId(projectConfig.categoryId);
        projectInfo.setKey(projectKey);
        projectInfo.setLeadAccountId(projectConfig.leadAccountId);

        projectInfo.setProjectTypeKey(projectConfig.projectTypeKey);

        if (flag.equals("P")){ //프로젝트
            projectName = "전자문서_프로젝트_WSS_"+ projectName +")";
            projectInfo.setName(projectName);

        }else{ // 유지보수
            projectName = "전자문서_유지보수_WSS_("+ projectName +")";
            projectInfo.setName(projectName);
        }

        WebClient webClient = WebClientUtils.createJiraWebClient(jiraConfig.baseUrl, jiraConfig.jiraID, jiraConfig.apiToken);
        String endpoint = "/rest/api/3/project";
        ProjectInfoData Response = WebClientUtils.post(webClient, endpoint, projectInfo, ProjectInfoData.class).block();

        Map<String, String> result = new HashMap<>();

        if(Response.getKey().isEmpty()){

            result.put(projectKey , "생성 실패");
        }

        return null;
    }



}
