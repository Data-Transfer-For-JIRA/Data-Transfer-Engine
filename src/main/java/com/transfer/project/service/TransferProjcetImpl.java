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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public Page<TB_PJT_BASE_Entity> getDataBaseProjectData(int pageIndex, int pageSize) throws Exception{

        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        Page<TB_PJT_BASE_Entity> page = TB_PJT_BASE_JpaRepository.findAllByOrderByBSSYSDATEDesc(pageable);

        return page;
    }

}
