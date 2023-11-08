package com.transfer.issue.service;

public interface TransferIssue {

    public String transferIssueData(String jiraKey, String projectCode) throws Exception;

    public String transferFirstIssue(String jiraKey, String projectCode) throws Exception;
}
