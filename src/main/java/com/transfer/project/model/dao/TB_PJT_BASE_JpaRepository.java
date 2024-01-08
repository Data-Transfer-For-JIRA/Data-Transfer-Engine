package com.transfer.project.model.dao;

import com.transfer.project.model.entity.TB_PJT_BASE_Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TB_PJT_BASE_JpaRepository extends JpaRepository<TB_PJT_BASE_Entity,String> {
    /*
    시간 기준 내림 차순 메서드 추가
    * */
    Page<TB_PJT_BASE_Entity> findAllByOrderByCreatedDateDesc(Pageable pageable);

    /*
    * 이관후
    * */
    Page<TB_PJT_BASE_Entity> findAllByMigrateFlagTrueOrderByCreatedDateDesc(Pageable pageable);
    Page<TB_PJT_BASE_Entity> findByProjectNameContainingAndMigrateFlagTrueOrderByCreatedDateDesc(String keyword,Pageable pageable);
    Page<TB_PJT_BASE_Entity> findAllByMigrateFlagTrueAndIssueMigrateFlagTrueOrderByCreatedDateDesc(Pageable pageable);

    /*
    이관전
    * */
    Page<TB_PJT_BASE_Entity> findAllByMigrateFlagFalseOrderByCreatedDateDesc(Pageable pageable);
    Page<TB_PJT_BASE_Entity> findByProjectNameContainingAndMigrateFlagFalseOrderByCreatedDateDesc(String keyword,Pageable pageable);

    /*
    정보 조회
    * */
    TB_PJT_BASE_Entity findByProjectCode(String projectCode);

    @Query("SELECT t.issueMigrateFlag FROM TB_PJT_BASE_Entity t WHERE t.projectCode = :projectCode")
    Boolean findIssueMigrateFlagByProjectCode(@Param("projectCode") String projectCode);
    /*  JPQL @Query 사용 이유: 특정 필드만 조회하기 위해 사용하였음
    * 1. Spring Data JPA의 메소드 이름으로 표현하기 어려운 복잡한 쿼리를 작성해야 할 때
      2. 특정 엔티티의 일부 필드만을 조회해야 할 때
      3. JPQL이나 native SQL을 사용하여 성능을 최적화하고자 할 때
    * */

    List<TB_PJT_BASE_Entity> findByRelatedProject(String projectCode);
    @Query("SELECT t.projectCode FROM TB_PJT_BASE_Entity t WHERE t.relatedProject = :relatedProject")
    List<String> findProjectCodesByRelatedProject(@Param("relatedProject")String relatedProject);

    @Query("SELECT t FROM TB_PJT_BASE_Entity t WHERE t.relatedProject IS NOT NULL AND t.relatedProject <> ''")
    List<TB_PJT_BASE_Entity> findNonNullAndNonEmptyRelatedProjects();


}
