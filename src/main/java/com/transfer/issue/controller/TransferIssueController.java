package com.transfer.issue.controller;

import com.transfer.issue.model.dto.CommentDTO;
import com.transfer.issue.model.dto.TransferIssueDTO;
import com.transfer.issue.model.dto.weblink.RequestWeblinkDTO;
import com.transfer.issue.model.dto.weblink.SearchWebLinkDTO;
import com.transfer.issue.service.TransferIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


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
            value = {""},
            method = {RequestMethod.POST}
    )
    public Map<String ,String> TransferIssueData(@RequestBody TransferIssueDTO transferIssueDTO ) throws Exception {
        logger.info("이슈 생성 컨트롤러 진입");
        return transferIssue.transferIssueData(transferIssueDTO);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/update"},
            method = {RequestMethod.PUT}
    )
    public Map<String, String> updateIssueData(@RequestBody TransferIssueDTO transferIssueDTO) throws Exception {
        logger.info("이슈 업데이트 컨트롤러 진입");
        return transferIssue.updateIssueData(transferIssueDTO);
    }


    @ResponseBody
    @RequestMapping(
            value = {"/weblink"},
            method = {RequestMethod.GET}
    )
    public List<SearchWebLinkDTO> getWebLinkByJiraKey(@RequestParam String jiraKey) throws Exception {
        logger.info("이슈 업데이트 컨트롤러 진입");
        return transferIssue.getWebLinkByJiraKey(jiraKey);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/add/weblink"},
            method = {RequestMethod.POST}
    )
    public String createWebLink(@RequestBody RequestWeblinkDTO requestWeblinkDTO) throws Exception {
        logger.info("이슈 업데이트 컨트롤러 진입");
        return transferIssue.createWebLink(requestWeblinkDTO);

    }

    @ResponseBody
    @RequestMapping(
            value = {"/delete/comment"},
            method = {RequestMethod.DELETE}
    )
    public void deleteComment(@RequestParam String issueIdOrKey, @RequestParam String id) throws Exception {
        logger.info("이슈 업데이트 컨트롤러 진입");
        transferIssue.deleteComment(issueIdOrKey,id);

    }

    @ResponseBody
    @RequestMapping(
            value = {"/get/comment"},
            method = {RequestMethod.GET}
    )
    public CommentDTO getComment(@RequestParam String issueIdOrKey) throws Exception {
        logger.info("이슈 업데이트 컨트롤러 진입");
        return transferIssue.getComment(issueIdOrKey);

    }


}


