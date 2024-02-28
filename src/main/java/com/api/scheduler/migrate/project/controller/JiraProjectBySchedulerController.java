package com.api.scheduler.migrate.project.controller;

import com.api.scheduler.migrate.project.service.JiraProjectByScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/*
*  스케줄러를 통해 프로젝트 생성 => 결과 값은 로그백을 이용해 로그파일로 생성
* */
@RestController
@RequestMapping("/jira/scheduler/migrate/project")
public class JiraProjectBySchedulerController {

    @Autowired
    private JiraProjectByScheduler jiraProjectByScheduler;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value = {""},
            method = {RequestMethod.POST}
    )
    public void transferProjectByScheduler(@RequestParam int project_count) throws Exception { // 바디로 변경
        logger.info("프로젝트 스케줄러를 통한 생성 컨트롤러 진입");
        jiraProjectByScheduler.createProject(project_count);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/assignee"},
            method = {RequestMethod.PUT}
    )
    public void reAssignProjectByScheduler() throws Exception {
        logger.info("프로젝트 스케줄러를 통한 생성 컨트롤러 진입");
        jiraProjectByScheduler.reAssignProjectByScheduler();
    }

    @ResponseBody
    @RequestMapping(
            value = {"/assignee/periodically"},
            method = {RequestMethod.PUT}
    )
    public void reAssignProjectBySchedulerPeriodically() throws Exception {
        logger.info("프로젝트 스케줄러를 통한 생성 컨트롤러 진입");
        jiraProjectByScheduler.reAssignProjectBySchedulerPeriodically();
    }

    @ResponseBody
    @RequestMapping(
            value = {"/assignee/date"},
            method = {RequestMethod.POST}
    )
    public void reAssignProjectBySchedulerWithDate(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) throws Exception {
        logger.info("프로젝트 스케줄러를 통한 특정 일자 이후 담당자 지정 컨트롤러 진입");
        jiraProjectByScheduler.reAssignProjectBySchedulerWithDate(date);
    }

}
