package com.admininfo.controller;

import com.admininfo.dto.AdminInfoDTO;
import com.admininfo.service.AdminInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/admin")
public class AdminInfroController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private AdminInfo adminInfo;

    @ResponseBody
    @RequestMapping(
            value = {"/info/{personalId}"},
            method = {RequestMethod.GET}
    )
    public AdminInfoDTO GetAdminInfo(@PathVariable int personalId) throws Exception {

        logger.info("사용자 조회");

        return adminInfo.getAdminInfo(personalId);
    }


}
