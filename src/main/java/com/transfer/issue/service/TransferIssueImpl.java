package com.transfer.issue.service;


import com.account.dto.AdminInfoDTO;
import com.account.service.Account;
import com.transfer.issue.model.dto.CreateIssueDTO;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@AllArgsConstructor
@Service("transferIssue")
public class TransferIssueImpl implements TransferIssue {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Account account;

    @Override
    public String transferIssueData(CreateIssueDTO createIssueDTO) throws Exception {
        logger.info("이슈 생성 시작");

        // 최초 이슈 생성
        Boolean isSuccess = createFirstIssue(createIssueDTO);

        // 나머지 이슈 벌크로 생성
        if(isSuccess){
            createBulkIssue(createIssueDTO);
        }


        return null;
    }

    public Boolean createFirstIssue(CreateIssueDTO createIssueDTO){
        logger.info("최초 이슈 생성");


        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint = "/rest/api/3/issue";


        return true;
    }

    public String createBulkIssue(CreateIssueDTO createIssueDTO){
        logger.info("최초 이슈 생성");

        return "1";
    }

}
