package com.jira.issue.controller;

import com.jira.issue.model.dto.TransferIssueDTO;
import com.jira.issue.model.dto.comment.CommentDTO;
import com.jira.issue.model.dto.search.*;
import com.jira.issue.model.dto.weblink.SearchWebLinkDTO;
import com.jira.issue.service.JiraIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/jira/issue")
public class JiraIssueController {

    @Autowired
    private JiraIssue jiraIssue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*
    * 이슈 조회
    * */
    @ResponseBody
    @RequestMapping(
            value = {""},
            method = {RequestMethod.GET}
    )
    public SearchRenderedIssue 이슈조회_컨트롤러(@RequestParam String 이슈_키 ) throws Exception {
        logger.info("이슈 조회 컨트롤러 진입");
        return jiraIssue.이슈_조회(이슈_키);
    }

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

    /*
     *  기본정보 이슈 업데이트시 사용 API - 현재 미사용
     * */
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
     *  유지보수_기본정보 이슈 조회 API
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
     *  프로젝트_기본정보 이슈 조회 API
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/project/base-info"},
            method = {RequestMethod.GET}
    )
    public SearchIssueDTO<SearchProjectInfoDTO> getProjectIssue(@RequestParam String issueKey) throws Exception{
        logger.info(":: JiraIssueController :: getProjectIssue");
        return jiraIssue.getProjectIssue(issueKey);
    }

    /*
    *  웹링크 조회 API
    * */
    @ResponseBody
    @RequestMapping(
            value = {"/weblink"},
            method = {RequestMethod.GET}
    )
    public List<SearchWebLinkDTO> getWebLinks(@RequestParam String issueKey) throws Exception{
        logger.info(":: JiraIssueController :: getWebLinks 웹링크 조회");
        return jiraIssue.getWebLinkByJiraIssueKey(issueKey);
    }

    /*
     *  오늘_업데이트및_생성된이슈들 조회 API
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/today"},
            method = {RequestMethod.GET}
    )
    public 오늘_생성및_업데이트된_이슈데이터 오늘_업데이트및_생성된이슈들() throws Exception{
        logger.info(":: JiraIssueController :: 오늘_업데이트및_생성된이슈들");
        return jiraIssue.오늘_업데이트및_생성된이슈들();
    }

    /*
    *  프로젝트에 생성된 이슈 조회 API
    * */
    @ResponseBody
    @RequestMapping(
            value = {"/project/all-issues"},
            method = {RequestMethod.GET}
    )
    public 프로젝트에_생성된_이슈데이터 프로젝트에_생성된_이슈데이터(@RequestParam String 지라프로젝트_키 ,
                                           @RequestParam int 검색_시작_지점 ,
                                           @RequestParam int 검색_최대_개수 ) throws Exception {

        logger.info("프로젝트에_생성된_이슈데이터");

        return jiraIssue.프로젝트에_생성된_이슈조회(지라프로젝트_키,검색_시작_지점,검색_최대_개수);
    }

    /*
    *  이슈 코멘트 조회
    * */
    @ResponseBody
    @RequestMapping(
            value = {"/comment"},
            method = {RequestMethod.GET}
    )
    public CommentDTO 이슈에_생성된_댓글조회(@RequestParam String 지라_이슈_아이디) throws Exception {

        logger.info("이슈에_생성된_댓글조회");

        return jiraIssue.이슈에_생성된_댓글조회(지라_이슈_아이디);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/today/comment"},
            method = {RequestMethod.GET}
    )
    public CommentDTO 오늘_업데이트및_생성된댓글들(@RequestParam String 지라_이슈_아이디) throws Exception {

        logger.info("오늘_업데이트및_생성된댓글들");

        return jiraIssue.오늘_업데이트및_생성된댓글들(지라_이슈_아이디);
    }
}


