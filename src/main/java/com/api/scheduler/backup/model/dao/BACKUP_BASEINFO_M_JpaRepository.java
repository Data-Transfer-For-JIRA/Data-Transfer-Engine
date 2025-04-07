package com.api.scheduler.backup.model.dao;

import com.api.scheduler.backup.model.entity.BACKUP_BASEINFO_M_Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface BACKUP_BASEINFO_M_JpaRepository extends JpaRepository<BACKUP_BASEINFO_M_Entity,String> {
}
