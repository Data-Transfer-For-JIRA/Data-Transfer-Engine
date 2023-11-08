package com.account.dao;

import com.account.entity.TB_ADMIN_Entity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TB_ADMIN_JpaRepository extends JpaRepository<TB_ADMIN_Entity, Integer> {
}
