package com.transfer.project.model.dao;

import com.transfer.project.model.entity.TB_JLL_Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TB_JLL_JpaRepository extends JpaRepository<TB_JLL_Entity,String> {

    List<TB_JLL_Entity> findAllByParentKeyAndLinkCheckFlagFalse(String parentKey);

    @Query("SELECT e.childKey FROM TB_JLL_Entity e WHERE e.parentKey = :parentKey AND e.linkCheckFlag = false")
    List<String> findChildKeysByParentKeyAndLinkCheckFlagFalse(@Param("parentKey") String parentKey);
    Page<TB_JLL_Entity> findAllByLinkCheckFlagIsFalse(Pageable pageable);

}