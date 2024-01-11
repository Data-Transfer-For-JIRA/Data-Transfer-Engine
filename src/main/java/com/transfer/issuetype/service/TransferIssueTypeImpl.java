package com.transfer.issuetype.service;

import com.transfer.issuetype.model.dto.IssueTypeSchemeDTO;
import com.utils.ProjectConfig;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service("transferIssueType")
public class TransferIssueTypeImpl implements TransferIssueType{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WebClientUtils webClientUtils;

    @Autowired
    private ProjectConfig projectConfig;


    @Override
    public void setIssueType(IssueTypeSchemeDTO issueTypeSchemeDTO , String flag) throws Exception{

        if(flag.equals("P")){
            issueTypeSchemeDTO.setIssueTypeSchemeId(projectConfig.projectIssueTypeScheme);
        }else{
            issueTypeSchemeDTO.setIssueTypeSchemeId(projectConfig.maintenanceIssueTypeScheme);
        }

        String endpoint = "/rest/api/2/issuetypescheme/project";
        webClientUtils.put(endpoint, issueTypeSchemeDTO, void.class).block();

    }

    @Override
    public void setIssueTypeScheduler()throws Exception{

    }
}
