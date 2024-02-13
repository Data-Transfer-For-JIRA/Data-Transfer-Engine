package com.jira.issue.controller;

import com.jira.issue.model.dto.TransferIssueDTO;
import com.jira.issue.service.JiraIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/jira/issue")
public class JiraIssueController {

    @Autowired
    private JiraIssue jiraIssue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /*
     * 해당 프로젝트에 이슈 생성
     * */
    @ResponseBody
    @RequestMapping(
            value = {""},
            method = {RequestMethod.POST}
    )
    public Map<String ,String> TransferIssueData(@RequestBody TransferIssueDTO transferIssueDTO ) throws Exception {
        logger.info("이슈 생성 컨트롤러 진입");
        return jiraIssue.transferIssueData(transferIssueDTO);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/update"},
            method = {RequestMethod.PUT}
    )
    public Map<String, String> updateIssueData(@RequestBody TransferIssueDTO transferIssueDTO) throws Exception {
        logger.info("이슈 업데이트 컨트롤러 진입");
        return jiraIssue.updateIssueData(transferIssueDTO);
    }

}


