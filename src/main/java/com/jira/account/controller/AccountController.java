package com.jira.account.controller;

import com.jira.account.dto.AdminInfoDTO;
import com.jira.account.dto.UserInfoDTO;
import com.jira.account.service.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/jira/admin")
public class AccountController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private Account account;

    @ResponseBody
    @RequestMapping(
            value = {"/info/{personalId}"},
            method = {RequestMethod.GET}
    )
    public AdminInfoDTO getAdminInfo(@PathVariable int personalId) throws Exception {

        logger.info("사용자 조회");

        return account.getAdminInfo(personalId);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/users"},
            method = {RequestMethod.GET}
    )
    public Flux<UserInfoDTO> getCollectUserInfo() throws Exception {

        logger.info("사용자 조회");

        return account.getCollectUserInfo();
    }


}
