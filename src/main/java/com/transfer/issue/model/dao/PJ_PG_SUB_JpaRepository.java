package com.transfer.issue.model.dao;

import com.transfer.issue.model.entity.PJ_PG_SUB_Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PJ_PG_SUB_JpaRepository extends JpaRepository<PJ_PG_SUB_Entity,String> {

}
