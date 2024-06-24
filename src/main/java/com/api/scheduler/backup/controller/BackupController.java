package com.api.scheduler.backup.controller;

import com.api.scheduler.backup.service.BackupScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/*
*  프로젝트는 플랫폼을 통해 JML에 생성하기 때문에 백업 대상 정보는 지라 프로젝트 이름, 담당자 이름
* */
@RestController
@RequestMapping("/api/scheduler/backup")
public class BackupController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    BackupScheduler backupScheduler;

    /*
    *  JML 테이블에 지라 프로젝트 리더 백업 하는 스케줄러 API
    * */
    @ResponseBody
    @RequestMapping(
            value={"/project/leader"},
            method={RequestMethod.PUT}
    )
    public void 지라프로젝트리더_백업() throws Exception{

        logger.info("[::BackupProjectController::] 지라 프로젝트 리더 백업 스케줄러");

        backupScheduler.updateJMLProjectLeader();

    }

    @ResponseBody
    @RequestMapping(
            value={"/project"},
            method={RequestMethod.PUT}
    )
    public void 지라프로젝트_백업() throws Exception{

        logger.info("[::BackupController::] 지라 프로젝트 백업 스케줄러");

        backupScheduler.지라프로젝트_백업();
    }

    /*
     *  기본 정보 저장 스케줄러
     * */
    @ResponseBody
    @RequestMapping(
            value={"/baseinfo"},
            method={RequestMethod.PUT}
    )
    public void 지라기본정보_백업() throws Exception{

        logger.info("[::BackupController::] 지라 기본정보 백업 스케줄러");

        backupScheduler.지라기본정보_백업();
    }

    /*
    *  기본 정보 저장 1개만
    * */
    @ResponseBody
    @RequestMapping(
            value = {"/baseissue"},
            method = {RequestMethod.PUT}
    )
    public Boolean 기본정보이슈_저장(@RequestParam String jiraKey, @RequestParam String projectType) throws Exception {
        logger.info("기본정보 이슈 저장 컨트롤러 진입");
        return backupScheduler.기본정보이슈_저장(jiraKey,projectType);
    }


    @ResponseBody
    @RequestMapping(
            value={"/issue"},
            method={RequestMethod.PUT}
    )
    public void 지라이슈_백업() throws Exception{

        logger.info("[::BackupController::] 지라 이슈 백업 스케줄러");

        backupScheduler.지라이슈_백업();
    }
}
