package com.api.scheduler.backup.service;

public interface BackupScheduler {
    void updateJMLProjectLeader() throws Exception;

    void 지라프로젝트_백업() throws Exception;

    void 지라기본정보_백업() throws Exception;

    void 지라이슈_백업() throws Exception;
}
