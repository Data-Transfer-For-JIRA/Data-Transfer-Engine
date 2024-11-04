package com.api.scheduler.backup.service;

import com.jira.project.model.dao.TB_JML_JpaRepository;
import com.jira.project.model.entity.TB_JML_Entity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class BackupSchedulerImplTest {

    @Autowired
    private BackupSchedulerImpl backupScheduler;

    @Autowired
    private TB_JML_JpaRepository TB_JML_JpaRepository;

    @Test
    @Transactional
    @Commit
    void upDateProjectNamePrefix() throws Exception {

        TB_JML_Entity 프로젝트 = TB_JML_JpaRepository.findByKey("TED833");

        backupScheduler.prefixUpdate(프로젝트);

        TB_JML_Entity 프로젝트_수정 = TB_JML_JpaRepository.findByKey("TED833");

        assertEquals("M_20241028 프로젝트 생성", 프로젝트_수정.getJiraProjectName());
    }
}