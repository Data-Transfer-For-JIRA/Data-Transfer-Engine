package com.scheduler.issue.controller;

import com.scheduler.issue.service.TransferIssueByScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scheduler/issue")
public class TransferIssueBySchedulerCotroller {

    @Autowired
    public TransferIssueByScheduler transferIssueByScheduler;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*
     *  댓글로 연관된 프로젝트 정보 입력해주는 컨트롤러
     * */
    @ResponseBody
    @RequestMapping(
            value = {""},
            method = {RequestMethod.POST}
    )
    public void TransferIssueByScheduler(@RequestBody int project_count) throws Exception {
        logger.info("스케줄러로 프로젝트에 생성된 이슈 지라로 이관 컨틀롤러");


    }


    /*
    *  댓글로 연관된 프로젝트 정보 입력해주는 컨트롤러
    * */
    @ResponseBody
    @RequestMapping(
            value = {"/link"},
            method = {RequestMethod.POST}
    )
    public void LinkProjectCodeByScheduler() throws Exception {
        logger.info("스케줄러로 연관된 프로젝트 정보 해당 이슈에 입력 컨틀롤러");
        transferIssueByScheduler.linkProjectCodeByScheduler();

    }
}
