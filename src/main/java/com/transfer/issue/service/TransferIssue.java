package com.transfer.issue.service;

import com.transfer.issue.model.dto.CreateIssueDTO;

public interface TransferIssue {

    public String transferIssueData(CreateIssueDTO createIssueDTO) throws Exception;

}
