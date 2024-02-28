package com.api.scheduler.migrate.project.service;

import java.util.Date;

public interface JiraProjectByScheduler {
    public void createProject(int project_count) throws Exception;

    public void reAssignProjectByScheduler() throws Exception;

    public void reAssignProjectBySchedulerPeriodically() throws Exception;

    public void reAssignProjectBySchedulerWithDate(Date date) throws Exception;
}
