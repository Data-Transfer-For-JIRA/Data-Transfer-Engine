package com.account.controller;

import com.account.dto.AdminInfoDTO;
import com.account.dto.LoginDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @ResponseBody
    @RequestMapping(
            value = {"/temp"},
            method = {RequestMethod.GET}
    )
    public Boolean tempLogin(@RequestBody LoginDTO loginDTO) throws Exception {
        logger.info("[::LoginController::] 임시 로그인");

        String id = loginDTO.getId();
        String pwd = loginDTO.getPwd();
        
        String adminId = "markany";
        String adminPwd = "markany";
        
        if(id.equals(adminId) && pwd.equals(adminPwd)){
            logger.info("[::LoginController::] 로그인 성공");
            return true;
        }else {
            logger.info("[::LoginController::] 로그인 실패");
            return false;
        }

    }
}
