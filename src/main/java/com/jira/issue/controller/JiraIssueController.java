package com.jira.issue.controller;

import com.jira.issue.model.dto.TransferIssueDTO;
import com.jira.issue.model.dto.search.SearchIssueDTO;
import com.jira.issue.model.dto.search.SearchMaintenanceInfoDTO;
import com.jira.issue.model.dto.search.SearchProjectInfoDTO;
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
    public Map<String ,String> transferIssueData(@RequestBody TransferIssueDTO transferIssueDTO ) throws Exception {
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

    /*
     *  유지보수_기본정보 이슈 조회
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/maintenance"},
            method = {RequestMethod.GET}
    )
    public SearchIssueDTO<SearchMaintenanceInfoDTO> getMaintenanceIssue(@RequestParam String issueKey) throws Exception{
        logger.info(":: JiraIssueController :: getMaintenanceIssue");
        return jiraIssue.getMaintenanceIssue(issueKey);
    }
    /*
     *  프로젝트_기본정보 이슈 조회
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/project"},
            method = {RequestMethod.GET}
    )
    public SearchIssueDTO<SearchProjectInfoDTO> getProjectIssue(@RequestParam String issueKey) throws Exception{
        logger.info(":: JiraIssueController :: getProjectIssue");
        return jiraIssue.getProjectIssue(issueKey);
    }

}


