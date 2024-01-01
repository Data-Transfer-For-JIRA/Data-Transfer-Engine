package com.scheduler.issue.service;

import java.util.Date;

public interface TransferIssueByScheduler {

    public void createIssueByScheduler() throws Exception;

    public void periodicallyCreateIssueByScheduler() throws Exception;

    public void transferIssueByDate(Date date) throws Exception;
}
