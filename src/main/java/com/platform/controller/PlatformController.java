package com.platform.controller;

import com.platform.dto.BaseDTO;
import com.platform.dto.ReturnMessage;
import com.platform.service.PlatformProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/platform")
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

    @ResponseBody
    @RequestMapping(
            value = {"/service"},
            method = {RequestMethod.POST}
    )
    public Map<String, String> platformService(@RequestBody BaseDTO baseDTO) throws Exception {
        logger.info("[::PlatformController::] 플랫폼을 통한 프로젝트 생성 및 이슈 생성");
        return platformProject.platformService(baseDTO);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/weblink"},
            method = {RequestMethod.PUT}
    )
    public ReturnMessage platformWeblink(@RequestParam String mainJiraKey , @RequestParam String subJiraKey) throws Exception {
        logger.info("[::PlatformController::] 플랫폼을 통한 웹링크 생성");
        return platformProject.platformWeblink(mainJiraKey,subJiraKey);
    }
    
}
