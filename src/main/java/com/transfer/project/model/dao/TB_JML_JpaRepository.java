package com.transfer.project.model.dao;

import com.transfer.project.model.entity.TB_JML_Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TB_JML_JpaRepository extends JpaRepository<TB_JML_Entity,String> {

    TB_JML_Entity findTopByOrderByMigratedDateDesc();

    Page<TB_JML_Entity> findByWssProjectNameContainingOrderByMigratedDateDesc(String keyword, Pageable pageable);

    Page<TB_JML_Entity> findAllByOrderByMigratedDateDesc(Pageable pageable);


    TB_JML_Entity findByProjectCode(String projectCode);

}
