package com.transfer.issue.model.dao;

import com.transfer.issue.model.entity.PJ_PG_SUB_Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PJ_PG_SUB_JpaRepository extends JpaRepository<PJ_PG_SUB_Entity, PJ_PG_SUB_Entity.ProjectId> {

    List<PJ_PG_SUB_Entity> findAllByProjectCodeOrderByCreationDateAsc(String projectCode);
    List<PJ_PG_SUB_Entity> findAllByProjectCodeOrderByCreationDateDesc(String projectCode);

    List<PJ_PG_SUB_Entity> findByCreationDate(Date creationDate);
}
