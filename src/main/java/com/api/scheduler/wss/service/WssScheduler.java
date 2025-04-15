package com.api.scheduler.wss.service;

import com.jira.issue.model.entity.PJ_PG_SUB_Entity;
import com.jira.project.model.entity.TB_PJT_BASE_Entity;

import java.util.List;

public interface WssScheduler {

    // =================================================프로젝트===========================================================
    
    void syncAllProjectData() throws Exception;

    TB_PJT_BASE_Entity syncSingleProject(String jiraProjectKey) throws Exception;


    // =================================================이슈===========================================================

    List<PJ_PG_SUB_Entity>  syncSingleIssue(String jiraProjectKey) throws Exception;

    List<PJ_PG_SUB_Entity>  syncAllIssue() throws Exception;
    
    
    // =================================================스케줄러===========================================================

    void syncProjectByScheduler() throws Exception;

    void syncIssueByScheduler() throws Exception;
}
