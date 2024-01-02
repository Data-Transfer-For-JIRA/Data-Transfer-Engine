package com.platform.controller;

import com.platform.dto.AssignProjectDTO;
import com.platform.dto.BaseDTO;
import com.platform.service.PlatformProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/platform")
public class PlatformController {

    @Autowired
    private PlatformProject platformProject;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value = {"/project"},
            method = {RequestMethod.POST}
    )
    public Map<String, String> platformCreateProject(@RequestBody BaseDTO baseDTO) throws Exception {
        logger.info("[::PlatformController::] 플랫폼을 통한 프로젝트 생성");
        return platformProject.platformCreateProject(baseDTO);
    }
    /*
     *  생성된 프로젝트의 담당자를 변경하는 컨틀롤러
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/assignee"},
            method = {RequestMethod.PUT}
    )
    public Map<String,String> platformAssignProject(@RequestBody AssignProjectDTO assignProjectDTO) throws Exception {
        logger.info("[::PlatformController::] 플랫폼을 통한 프로젝트 담당자 지정 변경");
        return  platformProject.platformAssignProject(assignProjectDTO);
    }


}
