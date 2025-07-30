package com.api.scheduler.backup.model.dao;

import com.api.scheduler.backup.model.entity.BACKUP_ISSUE_Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
public interface BACKUP_ISSUE_JpaRepository extends JpaRepository<BACKUP_ISSUE_Entity,String> {

    Page<BACKUP_ISSUE_Entity> findByJiraProjectKeyOrderByCreateDate(String 지라_프로젝트_키, Pageable pageable);

    List<BACKUP_ISSUE_Entity> findByJiraProjectKey(String 지라_프로젝트_키);

    List<BACKUP_ISSUE_Entity> findByCreateDateBetween(Date  startOfDay, Date endOfDay);
}
