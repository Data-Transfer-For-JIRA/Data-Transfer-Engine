package com.api.scheduler.migrate.issue.service;

import java.util.Date;

public interface JiraIssueByScheduler {

    public void createIssueByScheduler() throws Exception;

    public void periodicallyCreateIssueByScheduler() throws Exception;

    public void transferIssueByDate(Date date) throws Exception;

    void updateIssueByScheduler(int page, int size) throws Exception;

    public void updateWebLink(int size) throws Exception;

    boolean checkMigrateIssueFlag(String projectId, String projectCode);
}
