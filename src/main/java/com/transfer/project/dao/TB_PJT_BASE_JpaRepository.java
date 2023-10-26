package com.transfer.project.dao;

import com.transfer.project.model.TB_PJT_BASE_Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TB_PJT_BASE_JpaRepository extends JpaRepository<TB_PJT_BASE_Entity,Long> {
}
