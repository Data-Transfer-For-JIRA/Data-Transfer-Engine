package com.jira.account.model.dao;

import com.jira.account.model.entity.TB_JIRA_USER_Entity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TB_JIRA_USER_JpaRepository extends JpaRepository<TB_JIRA_USER_Entity,Integer> {
    TB_JIRA_USER_Entity findByDisplayName(String displayName);

    //TB_JIRA_USER_Entity findByDisplayNameContaining(String user);

    List<TB_JIRA_USER_Entity> findByDisplayNameContaining(String user);

    TB_JIRA_USER_Entity findByAccountId(String accountId);
}
