package com.api.scheduler.backup.project.controller;

import com.api.scheduler.backup.project.service.BackupProjectScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler/backup/project")
public class BackupProjectController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    BackupProjectScheduler backupProjectScheduler;

    /*
    *  JML 테이블에 지라 프로젝트 리더 백업 하는 스케줄러 API
    * */
    @ResponseBody
    @RequestMapping(
            value={"/leader"},
            method={RequestMethod.PUT}
    )
    public void 지라프로젝트리더_백업() throws Exception{

        logger.info("[::BackupProjectController::] 지라 프로젝트 리더 백업 스케줄러");

        backupProjectScheduler.updateJMLProjectLeader();

    }

}
