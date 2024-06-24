package com.api.scheduler.backup.service;

public interface BackupScheduler {
    void updateJMLProjectLeader() throws Exception;

    void 지라프로젝트_백업() throws Exception;

    void 지라기본정보_백업() throws Exception;

    Boolean 기본정보이슈_저장(String 지라_키,String 프로젝트_유형) throws Exception;
    void 지라이슈_백업() throws Exception;
}
