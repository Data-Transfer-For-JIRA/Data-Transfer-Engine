package com.transfer.project.service;

import com.utils.JiraConfig;
import com.transfer.project.model.ProjectData;
import com.transfer.project.model.ProjectInfo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service("transferProjcet")
public class TransferProjcetImpl implements TransferProjcet{

    @Autowired
    private JiraConfig jiraConfig;


    @Override
    public String createProject(ProjectInfo projectInfo) throws Exception{

        String project = projectInfo.getName();

        final WebClient jiraWebClient = jiraConfig.getJiraWebClient();


        String endpoint = "/rest/api/3/project";

        Mono<String> response = jiraWebClient.post()
                .uri(endpoint)
                .bodyValue(projectInfo)
                .retrieve()
                .bodyToMono(String.class);

        String jsonResponse = response.block();

        return jsonResponse;
    }
}
