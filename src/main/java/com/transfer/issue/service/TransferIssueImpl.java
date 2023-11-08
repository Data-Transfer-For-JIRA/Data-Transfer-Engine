package com.transfer.issue.service;


import com.account.dto.AdminInfoDTO;
import com.account.service.Account;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@AllArgsConstructor
@Service("transferIssue")
public class TransferIssueImpl implements TransferIssue {

    @Autowired
    private Account account;

    @Override
    public String transferIssueData(String jiraKey, String projectCode) throws Exception {
        return null;
    }

    @Override
    public String transferFirstIssue(String jiraKey, String projectCode) throws Exception {

        AdminInfoDTO info = account.getAdminInfo(1);

        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint = "/rest/api/3/issue";


        return null;
    }


}
