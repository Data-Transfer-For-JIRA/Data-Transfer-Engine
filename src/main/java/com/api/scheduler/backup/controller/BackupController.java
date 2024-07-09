package com.api.scheduler.backup.controller;

import com.api.scheduler.backup.service.BackupScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduler/backup")
public class BackupController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    BackupScheduler backupScheduler;

    /*------------------------------------젠킨스 스케줄러에 등록되는 API------------------------------------*/
    /*
    *  프로젝트 정보(담당자,이름) 백업 스케줄러 API
    *  지라에서 수정한 내용을 반영하기 위함
    * */
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
     *  기본 정보 벌크 백업 스케줄러 API ( 1주에 1번 수행 )
     *  대량으로 기본정보 이슈 데이터를 백업하기 위함( 지라에서 수정한 내용을 백업 ) - 데일리 백업에서 누락된 데이터 백업을 위함
     * */
    @ResponseBody
    @RequestMapping(
            value={"/bulk/base-issue"},
            method={RequestMethod.PUT}
    )
    public void 지라기본정보_벌크_백업() throws Exception{

        logger.info("[::BackupController::] 지라 기본정보 벌크 백업 스케줄러");

        long 시작시간 = System.currentTimeMillis();

        backupScheduler.지라기본정보_벌크_백업();

        long 종료시간 = System.currentTimeMillis();

        long 소요시간 = 종료시간 - 시작시간;

        logger.info("[::BackupController::] 지라 기본정보 벌크 백업 스케줄러 종로 소요시간: {}",소요시간);
    }

    /*
     *  이슈 벌크 백업 스케줄러 API ( 1주에 1번 수행 )
     *  대량으로 기본정보 이슈 데이터를 백업하기 위함( 지라에서 수정한 내용을 백업 ) - 데일리 백업에서 누락된 데이터 백업을 위함
     * */
    @ResponseBody
    @RequestMapping(
            value={"/bulk/normal-issue"},
            method={RequestMethod.PUT}
    )
    public void 지라이슈_벌크_백업() throws Exception{

        logger.info("[::BackupController::] 지라 이슈 벌크 백업 스케줄러");

        backupScheduler.지라이슈_벌크_백업();
    }
    /*
     *  당일에 이루어진 이슈 백업 스케줄러 API ( 하루에 한번 수행 )
     *  당일 생성 및 업데이트 된 정보 백업하기 위함 ( 기본 정보 이슈, 일반 이슈 데이터 백업 )
     * */
    @ResponseBody
    @RequestMapping(
            value={"/daily/issue"},
            method={RequestMethod.PUT}
    )
    public void 지라이슈_데일리_백업() throws Exception{

        logger.info("[::BackupController::] 지라 이슈 데일리 백업 스케줄러");

        backupScheduler.지라이슈_데일리_백업();
    }

    /*------------------------------------젠킨스 스케줄러에 등록되지 않는 API------------------------------------*/
    /*
     *  기본 정보 1개만 백업 API
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/baseissue"},
            method = {RequestMethod.PUT}
    )
    public Boolean 기본정보이슈_저장(@RequestParam String jiraKey, @RequestParam String projectType) throws Exception {

        logger.info("[::BackupController::] 지라 기본정보 백업 스케줄러");

        return backupScheduler.기본정보이슈_저장(jiraKey,projectType);
    }
    /*
     *  JML 테이블에 지라 프로젝트 리더 백업 하는 스케줄러 API (지라에서 변경한 내용을 감지하기 위함)
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
}
