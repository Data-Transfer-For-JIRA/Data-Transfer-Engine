package com.transfer.issue.service;

import com.transfer.issue.model.dto.CreateIssueDTO;

import java.util.Map;

public interface TransferIssue {

    Map<String ,String> transferIssueData(String projectCode) throws Exception;

}
