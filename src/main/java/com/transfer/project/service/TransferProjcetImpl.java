package com.transfer.project.service;

import com.transfer.project.dao.TB_PJT_BASE_JpaRepository;
import com.transfer.project.model.ProjectInfoData;
import com.transfer.project.model.TB_PJT_BASE_Entity;
import com.utils.JiraConfig;
import com.transfer.project.model.ProjectCreateDTO;
import com.utils.ProjectConfig;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@AllArgsConstructor
@Service("transferProjcet")
public class TransferProjcetImpl implements TransferProjcet{

    @Autowired
    private JiraConfig jiraConfig;

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
    public List<TB_PJT_BASE_Entity> getDataBaseProjectData() throws Exception{

        return TB_PJT_BASE_JpaRepository.findAll().subList(0, Math.min(TB_PJT_BASE_JpaRepository.findAll().size(), 10));
    }

}
