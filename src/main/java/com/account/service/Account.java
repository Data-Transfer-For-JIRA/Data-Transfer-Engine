package com.account.service;

import com.account.dto.AdminInfoDTO;

import java.util.Map;

public interface Account {

    public  AdminInfoDTO getAdminInfo(int personalId);

    public Map<String,String> getCollectUserInfo();

}
