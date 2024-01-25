package com.api.project.model.dao;

import com.api.project.model.entity.TB_JML_Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TB_JML_JpaRepository extends JpaRepository<TB_JML_Entity,String> {

    TB_JML_Entity findTopByOrderByMigratedDateDesc();

    Page<TB_JML_Entity> findByWssProjectNameContainingOrderByMigratedDateDesc(String keyword, Pageable pageable);
    @Query("SELECT j FROM TB_JML_Entity j WHERE " +
            "j.key LIKE %:keyword% OR " +
            "j.projectCode LIKE %:keyword% OR " +
            "j.wssProjectName LIKE %:keyword%")
    Page<TB_JML_Entity> findByKeyOrProjectCodeOrWssProjectNameContaining(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    Page<TB_JML_Entity> findAllByOrderByMigratedDateDesc(Pageable pageable);

    Page<TB_JML_Entity> findAllByUpdateIssueFlagFalseOrderByMigratedDateDesc(Pageable pageable);

    TB_JML_Entity findByProjectCode(String projectCode);

    @Query("SELECT t FROM TB_JML_Entity t WHERE t.migratedDate >= :startDate AND t.migratedDate < :endDate")
    List<TB_JML_Entity> findProjectCodeByMigratedDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    Page<TB_JML_Entity> findAll(Specification<TB_JML_Entity> updateDate, Pageable pageable);

    TB_JML_Entity findByKey(String jiraKey);
}
