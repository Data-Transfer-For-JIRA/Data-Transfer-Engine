package com.jira.account.controller;

//import com.account.dto.AdminInfoDTO;
//import com.account.dto.JwtToken;
import com.jira.account.dto.LoginDTO;
//import com.account.service.Login;
//import com.account.service.LoginImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    @Autowired
//    private Login login;

    @ResponseBody
    @RequestMapping(
            value = {"/temp"},
            method = {RequestMethod.POST}
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

//    @ResponseBody
//    @RequestMapping(
//            value = {""},
//            method = {RequestMethod.POST}
//    )
//    public JwtToken login(@RequestBody LoginDTO loginDTO) {
//        String memberId = loginDTO.getId();
//        String password = loginDTO.getPwd();
//        JwtToken jwtToken = login.signIn(memberId, password);
//        return jwtToken;
//    }

    @ResponseBody
    @RequestMapping(
            value = {"/test"},
            method = {RequestMethod.GET}
    )
    public String jwtTest() {
        return "success";
    }
}
