package com.transfer.issue.service;


import com.admininfo.service.AdminInfo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service("transferIssue")
public class TransferIssueImpl implements TransferIssue {

    @Autowired
    private AdminInfo adminInfo;

    @Override
    public String transferIssuTypeeData(String jiraKey, String projectCode) throws Exception{
        return null;
    }

    @Override
    public String transferIssueData(String jiraKey, String projectCode) throws Exception {
        return null;
    }


}
