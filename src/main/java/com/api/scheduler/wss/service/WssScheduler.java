package com.api.scheduler.wss.service;

import com.jira.issue.model.entity.PJ_PG_SUB_Entity;
import com.jira.project.model.entity.TB_PJT_BASE_Entity;

import java.util.List;

public interface WssScheduler {

    void syncAllProjectData() throws Exception;

    TB_PJT_BASE_Entity syncSingleProject(String jiraProjectKey) throws Exception;

    void syncProjectByScheduler() throws Exception;


    // =================================================이슈===========================================================
    List<PJ_PG_SUB_Entity>  syncIssue(String jiraProjectKey) throws Exception;
}
