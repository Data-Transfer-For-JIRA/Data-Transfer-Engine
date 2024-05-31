package com.jira.issue.model.dao;

import com.jira.issue.model.entity.backup.BACKUP_BASEINFO_M_Entity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BACKUP_BASEINFO_M_JpaRepository extends JpaRepository<BACKUP_BASEINFO_M_Entity,String> {
}
