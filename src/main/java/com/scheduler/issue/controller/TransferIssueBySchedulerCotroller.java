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
     *  스케줄러로 생성된 프로젝트의 이슈를 생성해주는 컨트롤러
     * */
    @ResponseBody
    @RequestMapping(
            value = {""},
            method = {RequestMethod.POST}
    )
    public void TransferIssueByScheduler() throws Exception {
        logger.info("스케줄러로 프로젝트에 생성된 이슈 지라로 이관 컨틀롤러");
        transferIssueByScheduler.createIssueByScheduler();

    }

}
