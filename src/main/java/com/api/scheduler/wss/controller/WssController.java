package com.api.scheduler.wss.controller;

import com.api.scheduler.wss.service.WssScheduler;
import com.jira.issue.model.entity.PJ_PG_SUB_Entity;
import com.jira.project.model.entity.TB_PJT_BASE_Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler/wss")
public class WssController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WssScheduler wssScheduler;

    //=================================================프로젝트===========================================================


    // 수동: 지라에 생성된 프로젝트 중 WSS에 생성 안된 프로젝트 생성 전체 동기화
    @PostMapping("/sync/allProject")
    public void syncAllProjectData() throws Exception {
        logger.info("sync project data");
        wssScheduler.syncAllProjectData();
    }
    // 수동: 프로젝트 키 값으로 WSS에 생성 안된 프로젝트 생성 동기화
    @PostMapping("/sync/singleProject")
    public TB_PJT_BASE_Entity singleProject(@RequestParam String jiraProjectKey) throws Exception {
        logger.info("sync single project data");
        return wssScheduler.syncSingleProject(jiraProjectKey);
    }

    //=================================================이  슈===========================================================

    // 단일 프로젝트 동기화
    @PostMapping("/sync/singleIssue")
    public List<PJ_PG_SUB_Entity>  syncSingleIssue(@RequestParam String jiraProjectKey) throws Exception {
        logger.info("sync issue data");
        return wssScheduler.syncSingleIssue(jiraProjectKey);
    }

    // 전체 프로젝트 동기화
    @PostMapping("/sync/allIssue")
    public List<PJ_PG_SUB_Entity>  syncAllIssue() throws Exception {
        logger.info("sync issue data");
        return wssScheduler.syncAllIssue();
    }

    //=================================================스케줄러===========================================================

    // 스케줄러: 프로젝트 동기화 (일별)
    @PostMapping("/schedule/project")
    public void syncProjectByScheduler() throws Exception {
        logger.info("스케줄러를 통한 프로젝트 정보 WSS로 동기화");
        wssScheduler.syncProjectByScheduler();
    }


    // 스케줄러: 지라에 생성된 이슈 일별 동기화
    @PostMapping("/schedule/Issue")
    public void syncIssueByScheduler() throws Exception {
        logger.info("스케줄러를 통한 이슈 정보 WSS로 동기화");
        wssScheduler.syncIssueByScheduler();
    }

}
