package com.api.scheduler.backup.model.dao;

import com.api.scheduler.backup.model.entity.BACKUP_ISSUE_COMMENT_Entity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BACKUP_ISSUE_COMMENT_JpaRepository extends JpaRepository<BACKUP_ISSUE_COMMENT_Entity,String> {
}
