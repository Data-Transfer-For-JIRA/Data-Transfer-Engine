package com.jira.account.service;

import com.jira.account.dto.AdminInfoDTO;
import com.jira.account.dto.UserInfoDTO;
import reactor.core.publisher.Flux;


public interface Account {

    public AdminInfoDTO getAdminInfo(int personalId);

    public Flux<UserInfoDTO> getCollectUserInfo();

    String getUserNameByJiraAccountId(String accountId);
}
