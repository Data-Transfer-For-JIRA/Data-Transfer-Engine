package com.scheduler.issue.controller;

import com.scheduler.issue.service.TransferIssueByScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

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
    public void transferIssueByScheduler() throws Exception {
        logger.info("스케줄러로 프로젝트에 생성된 이슈 지라로 이관 컨트롤러");
        transferIssueByScheduler.createIssueByScheduler();
    }
    /*
     *  스케줄러로 wss에 생성한 이슈를 지라로 이관하는 컨트롤러 - 12월 28일 기준 이후에 생성된 이슈를 이관해 줌
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/periodically"},
            method = {RequestMethod.POST}
    )
    public void periodicallyTransferIssueByScheduler() throws Exception {
        logger.info("스케줄러로 wss에 생성한 이슈를 지라로 이관하는 컨트롤러 - 12월 28일 기준 이후에 생성된 이슈를 이관해 줌");
        transferIssueByScheduler.periodicallyCreateIssueByScheduler();
    }
    /*
     *  스케줄러로 특정일자 wss에 생성한 이슈를 지라로 이관하는 컨트롤러
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/date"},
            method = {RequestMethod.POST}
    )
    public void transferIssueByDate(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) throws Exception {
        logger.info("특정일자 wss에 생성한 이슈를 지라로 이관하는 컨트롤러");
        transferIssueByScheduler.transferIssueByDate(date);
    }



}
