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
     * 해당 프로젝트에 이슈 타입 데이터 생성
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/issuetype"},
            method = {RequestMethod.POST}
    )
    public String TransferIssuTypeeData(@RequestParam String jiraKey, @RequestParam String projectCode) throws Exception {

        logger.info("이슈 타입 연결");
        return transferIssue.transferIssuTypeeData(jiraKey,projectCode);
    }

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
}
