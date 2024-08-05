package com.api.platform.controller;

import com.api.platform.dto.BaseDTO;
import com.api.platform.dto.ReturnMessage;
import com.api.platform.service.PlatformProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
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
            value = {"/project"},
            method = {RequestMethod.GET}
    )
    public BaseDTO platformGetProject(@RequestParam String projectFlag,@RequestParam String jiraKey) throws Exception {
        logger.info("[::PlatformController::] 프로젝트 정보 상세 조회");
        return platformProject.platformGetProject(projectFlag, jiraKey);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/issue"},
            method = {RequestMethod.GET}
    )
    public BaseDTO platformGetIssue(@RequestParam String jiraIssueKey) throws Exception {
        logger.info("[::PlatformController::] 이슈 상세 조회");
        return platformProject.platformGetIssue(jiraIssueKey);
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
    public List<ReturnMessage> platformWeblink(@RequestParam String mainJiraKey , @RequestParam List<String> subJiraKeyList) throws Exception {

        logger.info("[::PlatformController::] 플랫폼을 통한 웹링크 생성");

        List<ReturnMessage> ReturnMessageList = new ArrayList<>();

        for(String subJiraKey : subJiraKeyList){

            ReturnMessage returnMessages=  platformProject.platformWeblink(mainJiraKey,subJiraKey);

            ReturnMessageList.add(returnMessages);
        }

        return ReturnMessageList;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/update"},
            method = {RequestMethod.PUT}
    )
    public void upDateProjectInfo(@RequestParam String jiraKey, @RequestBody BaseDTO baseDTO) throws Exception {
        logger.info("[::PlatformController::] 플랫폼 프로젝트 업데이트");
        platformProject.upDateProjectInfo(jiraKey, baseDTO);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/test/createTicket"},
            method = {RequestMethod.POST}
    )
    public Map<String, String> platformCreateTicket(@RequestParam(defaultValue = "테스트") String summary, @RequestBody String description) throws Exception {
        logger.info("[::PlatformController::] 테스트용 티켓 생성");
        return platformProject.createTicket(summary, description);
    }
}
