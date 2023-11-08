package com.transfer.issue.controller;

import com.transfer.issue.service.TransferIssue;
import com.transfer.project.model.dto.ProjectCreateDTO;
import com.transfer.project.model.dto.ProjectInfoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/transfer/issue")
public class TransferIssueController {

    @Autowired
    private TransferIssue transferIssue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /*
     * 해당 프로젝트에 이슈 생성
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/"},
            method = {RequestMethod.POST}
    )
    public String TransferIssueData(@RequestParam String jiraKey, @RequestParam String projectCode) throws Exception {
        logger.info("이슈 생성");
        return transferIssue.transferIssueData(jiraKey,projectCode);
    }

    /*
     * 최초 정보 이슈 생성 프로젝트 기본정보 이슈 타입으로 기본정보를 생성 해야한다.
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/first"},
            method = {RequestMethod.POST}
    )
    public String TransferFirstIssue(@RequestParam String jiraKey, @RequestParam String projectCode) throws Exception {
        logger.info("이슈 생성");
        return transferIssue.transferFirstIssue(jiraKey,projectCode);
    }
}


