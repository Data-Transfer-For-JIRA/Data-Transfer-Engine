package com.jira.project.model.dao;

import com.jira.project.model.entity.TB_JML_Entity;
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
    List<TB_JML_Entity> findByKeyOrProjectCodeOrWssProjectNameContaining(
            @Param("keyword") String keyword
    );

    Page<TB_JML_Entity> findAllByOrderByMigratedDateDesc(Pageable pageable);

    Page<TB_JML_Entity> findAllByUpdateIssueFlagFalseOrderByMigratedDateDesc(Pageable pageable);

    TB_JML_Entity findByProjectCode(String projectCode);

    @Query("SELECT t FROM TB_JML_Entity t WHERE t.migratedDate >= :startDate AND t.migratedDate < :endDate")
    List<TB_JML_Entity> findProjectCodeByMigratedDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    Page<TB_JML_Entity> findAll(Specification<TB_JML_Entity> updateDate, Pageable pageable);

    TB_JML_Entity findByKey(String jiraKey);

    @Query("SELECT j FROM TB_JML_Entity j " +
            "LEFT JOIN BACKUP_BASEINFO_P_Entity bp ON j.key = bp.지라_프로젝트_키 " +
            "LEFT JOIN BACKUP_BASEINFO_M_Entity bm ON j.key = bm.지라_프로젝트_키 " +
            "WHERE bp.계약사 LIKE %:keyword% OR bm.계약사 LIKE %:keyword%")
    List<TB_JML_Entity> findByContractorLike(@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM TB_JML WHERE M_DATE >= '2024-01-01'", nativeQuery = true)
    List<TB_JML_Entity> findProjectDateAfter2024();


    List<TB_JML_Entity> findByMigratedDateBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

}
