package com.transfer.issuetype.service;

import com.account.dto.AdminInfoDTO;
import com.account.service.Account;
import com.transfer.issuetype.model.dto.IssueTypeSchemeDTO;
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
    public void setIssueType(IssueTypeSchemeDTO issueTypeSchemeDTO , String flag) throws Exception{

        AdminInfoDTO info = account.getAdminInfo(1);

        if(flag.equals("P")){
            issueTypeSchemeDTO.setIssueTypeSchemeId(projectConfig.projectIssueTypeScheme);
        }else{
            issueTypeSchemeDTO.setIssueTypeSchemeId(projectConfig.maintenanceIssueTypeScheme);
        }

        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint = "/rest/api/2/issuetypescheme/project";
        WebClientUtils.put(webClient, endpoint, issueTypeSchemeDTO,void.class).block();

    }

    @Override
    public void setIssueTypeScheduler()throws Exception{

    }
}
