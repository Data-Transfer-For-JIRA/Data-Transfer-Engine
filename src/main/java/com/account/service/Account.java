package com.account.service;

import com.account.dto.AdminInfoDTO;
import com.account.dto.UserInfoDTO;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public interface Account {

    public AdminInfoDTO getAdminInfo(int personalId);

    public Flux<UserInfoDTO> getCollectUserInfo();

}
