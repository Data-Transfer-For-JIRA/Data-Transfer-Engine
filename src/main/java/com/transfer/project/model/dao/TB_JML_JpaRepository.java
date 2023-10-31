package com.transfer.project.model.dao;

import com.transfer.project.model.entity.TB_JML_Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TB_JML_JpaRepository extends JpaRepository<TB_JML_Entity,String> {

}
