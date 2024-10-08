package com.api.scheduler.backup.service;

import java.util.concurrent.CompletableFuture;

public interface BackupScheduler {

    CompletableFuture<Void> 지라_프로젝트이름_수정() throws Exception;

    /* 프로젝트 정보 백업 스케줄러*/
    CompletableFuture<Void> 지라프로젝트_백업() throws Exception;

    /* 기본정보 및 일반 이슈 데일리 백업*/
    void 지라이슈_데일리_백업() throws Exception;

    /* 기본 정보 벌크 백업 스케줄러*/
    CompletableFuture<Void> 지라기본정보_벌크_백업() throws Exception;
    /* 이슈 백업 벌크 스케줄러*/
    CompletableFuture<Void> 지라이슈_벌크_백업() throws Exception;

    void 지라이슈_저장(String 지라이슈_키) throws Exception;

    Boolean 기본정보이슈_저장(String 지라_키,String 프로젝트_유형) throws Exception;

    void updateJMLProjectLeader() throws Exception;
}
