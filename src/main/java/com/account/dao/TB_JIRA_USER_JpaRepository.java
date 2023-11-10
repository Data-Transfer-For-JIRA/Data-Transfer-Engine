package com.account.dao;

import com.account.dto.UserInfoDTO;
import com.account.entity.TB_JIRA_USER_Entity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TB_JIRA_USER_JpaRepository extends JpaRepository<TB_JIRA_USER_Entity,Integer> {
}
