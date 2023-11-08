package com.transfer.issuetype.service;

import com.account.dto.AdminInfoDTO;
import com.account.service.Account;
import com.transfer.issuetype.model.dto.IssueTypeConnectDTO;
import com.utils.ProjectConfig;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@AllArgsConstructor
@Service("transferIssueType")
public class TransferIssueTypeImpl implements TransferIssueType{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Account account;

    @Autowired
    private ProjectConfig projectConfig;


    @Override
    public void setIssueType(Integer projectId) throws Exception{

        AdminInfoDTO info = account.getAdminInfo(1);

        IssueTypeConnectDTO issueTypeConnectDTO = new IssueTypeConnectDTO();
        issueTypeConnectDTO.setIssueTypeSchemeId(projectConfig.issuetypeId);
        issueTypeConnectDTO.setProjectId(projectId);
        
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint = "/rest/api/3/issuetypescheme/project";
        WebClientUtils.put(webClient, endpoint,issueTypeConnectDTO,void.class).block();

    }

    @Override
    public void setIssueTypeScheduler()throws Exception{

    }
}
