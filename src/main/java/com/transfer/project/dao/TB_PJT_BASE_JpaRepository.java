package com.transfer.project.dao;

import com.transfer.project.model.TB_PJT_BASE_Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TB_PJT_BASE_JpaRepository extends JpaRepository<TB_PJT_BASE_Entity,String> {
    /*
    시간 기준 내림 차순 메서드 추가
    * */
    Page<TB_PJT_BASE_Entity> findAllByOrderByBSSYSDATEDesc(Pageable pageable);
}
