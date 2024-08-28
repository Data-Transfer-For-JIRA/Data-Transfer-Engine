package com.jira.issue.model.dao;

import com.jira.issue.model.entity.backup.BACKUP_ISSUE_Entity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BACKUP_ISSUE_JpaRepository extends JpaRepository<BACKUP_ISSUE_Entity,Long> {
}
