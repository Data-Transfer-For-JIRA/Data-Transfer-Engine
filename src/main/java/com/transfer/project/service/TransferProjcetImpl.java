package com.transfer.project.service;

import com.transfer.project.model.ProjectInfoData;
import com.utils.JiraConfig;
import com.transfer.project.model.ProjectCreateDTO;
import com.utils.ProjectConfig;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service("transferProjcet")
public class TransferProjcetImpl implements TransferProjcet{

    @Autowired
    private JiraConfig jiraConfig;

    @Autowired
    private ProjectConfig projectConfig;

    @Override
    public ProjectCreateDTO createProject(ProjectCreateDTO projectCreateDTO) throws Exception{
        String key = projectCreateDTO.getKey();
        String name = projectCreateDTO.getName();

        ProjectCreateDTO pd = new ProjectCreateDTO();

        pd.setKey(key);
        pd.setName(name);
        pd.setAssigneType(projectConfig.assigneType);
        pd.setCategoryId(Integer.parseInt(projectConfig.categoryId));
        pd.setLeadAccountId(projectConfig.leadAccountId);
        pd.setProjectTypeKey(projectConfig.projectTypeKey);
        pd.setUrl(projectConfig.url);
        pd.setDescription("안녕하세요");

        WebClient webClient = WebClientUtils.createJiraWebClient(jiraConfig.baseUrl, jiraConfig.jiraID,jiraConfig.apiToken);

        String endpoint = "/rest/api/3/project";

        ProjectCreateDTO Response= WebClientUtils.post(webClient,endpoint,projectCreateDTO,ProjectCreateDTO.class).block();


        return Response;
    }
}
