package com.transfer.issue.service;

public interface TransferIssue {

    public String transferIssuTypeeData(String jiraKey, String projectCode) throws Exception;

    public String transferIssueData(String jiraKey, String projectCode) throws Exception;
}
