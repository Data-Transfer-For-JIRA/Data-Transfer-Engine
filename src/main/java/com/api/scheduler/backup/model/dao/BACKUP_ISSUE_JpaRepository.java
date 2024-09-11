package com.api.scheduler.backup.model.dao;

import com.api.scheduler.backup.model.entity.BACKUP_ISSUE_Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BACKUP_ISSUE_JpaRepository extends JpaRepository<BACKUP_ISSUE_Entity,String> {

    Page<BACKUP_ISSUE_Entity> findByJiraProjectKeyOrderByCreateDate(String 지라_프로젝트_키, Pageable pageable);

}
