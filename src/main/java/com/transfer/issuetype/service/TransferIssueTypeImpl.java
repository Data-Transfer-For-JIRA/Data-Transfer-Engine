package com.transfer.issuetype.service;

import com.account.dto.AdminInfoDTO;
import com.account.service.Account;
import com.transfer.issuetype.model.dto.IssueTypeScreenSchemeDTO;
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
    public void setIssueType(IssueTypeScreenSchemeDTO issueTypeScreenSchemeDTO ,String flag) throws Exception{

        AdminInfoDTO info = account.getAdminInfo(1);

        if(flag.equals("P")){
            issueTypeScreenSchemeDTO.setIssueTypeScreenSchemeId(projectConfig.projectIssueType);
        }else{
            issueTypeScreenSchemeDTO.setIssueTypeScreenSchemeId(projectConfig.maintenanceIssueType);
        }

        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint = "/rest/api/3/issuetypescreenscheme/project";
        WebClientUtils.put(webClient, endpoint, issueTypeScreenSchemeDTO,void.class).block();

    }

    @Override
    public void setIssueTypeScheduler()throws Exception{

    }
}
