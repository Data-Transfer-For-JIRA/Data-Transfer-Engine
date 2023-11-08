package com.account.controller;

import com.account.dto.AdminInfoDTO;
import com.account.service.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/admin")
public class AccountController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private Account account;

    @ResponseBody
    @RequestMapping(
            value = {"/info/{personalId}"},
            method = {RequestMethod.GET}
    )
    public AdminInfoDTO GetAdminInfo(@PathVariable int personalId) throws Exception {

        logger.info("사용자 조회");

        return account.getAdminInfo(personalId);
    }


}
