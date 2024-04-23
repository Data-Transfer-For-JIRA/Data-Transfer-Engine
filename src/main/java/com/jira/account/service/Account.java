package com.jira.account.service;

import com.jira.account.model.dto.AdminInfoDTO;
import com.jira.account.model.dto.UserInfoDTO;
import reactor.core.publisher.Flux;


public interface Account {

    public AdminInfoDTO getAdminInfo(int personalId);

    public Flux<UserInfoDTO> getCollectUserInfo();

    String getUserNameByJiraAccountId(String accountId);
}
