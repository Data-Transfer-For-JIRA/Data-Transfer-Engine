package com.api.scheduler.migrate.project.service;

public interface JiraProjectByScheduler {
    public void createProject(int project_count) throws Exception;

    public void reAssgineProjectByScheduler() throws Exception;

    public void reAssgineProjectBySchedulerPeriodically() throws Exception;
}
