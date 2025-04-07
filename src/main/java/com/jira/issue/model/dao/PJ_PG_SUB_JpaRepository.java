package com.jira.issue.model.dao;

import com.jira.issue.model.entity.PJ_PG_SUB_Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PJ_PG_SUB_JpaRepository extends JpaRepository<PJ_PG_SUB_Entity, PJ_PG_SUB_Entity.ProjectId> {

    List<PJ_PG_SUB_Entity> findAllByProjectCodeOrderByCreationDateAsc(String projectCode);
    List<PJ_PG_SUB_Entity> findAllByProjectCodeOrderByCreationDateDesc(String projectCode);

    List<PJ_PG_SUB_Entity> findByCreationDate(Date creationDate);

    List<PJ_PG_SUB_Entity> findByIssueMigrateFlagIsFalse();

    // id와 code로 특정 이력 조회
    PJ_PG_SUB_Entity findByProjectIdAndProjectCode(String projectId, String projectCode);


    @Query("SELECT COALESCE(MAX(p.projectId), 0) FROM PJ_PG_SUB_Entity p WHERE p.projectCode = :projectCode")
    int findMaxProjectId(@Param("projectCode") String projectCode);

}
