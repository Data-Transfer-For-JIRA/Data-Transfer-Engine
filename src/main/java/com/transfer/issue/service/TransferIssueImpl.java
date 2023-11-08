package com.transfer.issue.service;


import com.account.service.Account;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service("transferIssue")
public class TransferIssueImpl implements TransferIssue {

    @Autowired
    private Account account;

    @Override
    public String transferIssuTypeeData(String jiraKey, String projectCode) throws Exception{
        return null;
    }

    @Override
    public String transferIssueData(String jiraKey, String projectCode) throws Exception {
        return null;
    }


}
