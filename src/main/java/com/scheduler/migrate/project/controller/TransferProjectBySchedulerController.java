package com.scheduler.migrate.project.controller;

import com.scheduler.migrate.project.service.TransferProjectByScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/*
*  스케줄러를 통해 프로젝트 생성 => 결과 값은 로그백을 이용해 로그파일로 생성
* */
@RestController
@RequestMapping("/scheduler/project")
public class TransferProjectBySchedulerController {

    @Autowired
    private TransferProjectByScheduler transferProjectByScheduler;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value = {""},
            method = {RequestMethod.POST}
    )
    public void transferProjectByScheduler(@RequestParam int project_count) throws Exception { // 바디로 변경
        logger.info("프로젝트 스케줄러를 통한 생성 컨트롤러 진입");
        transferProjectByScheduler.createProject(project_count);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/assignee"},
            method = {RequestMethod.PUT}
    )
    public void reAssgineProjectByScheduler() throws Exception {
        logger.info("프로젝트 스케줄러를 통한 생성 컨트롤러 진입");
        transferProjectByScheduler.reAssgineProjectByScheduler();
    }

    @ResponseBody
    @RequestMapping(
            value = {"/assignee/periodically"},
            method = {RequestMethod.PUT}
    )
    public void reAssgineProjectBySchedulerPeriodically() throws Exception {
        logger.info("프로젝트 스케줄러를 통한 생성 컨트롤러 진입");
        transferProjectByScheduler.reAssgineProjectBySchedulerPeriodically();
    }

}
