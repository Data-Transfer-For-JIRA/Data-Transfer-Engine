package com.transfer.issue.service;

import com.transfer.issue.model.dto.CreateIssueDTO;
import com.transfer.issue.model.dto.TransferIssueDTO;

import java.util.Map;

public interface TransferIssue {

    Map<String ,String> transferIssueData(TransferIssueDTO transferIssueDTO) throws Exception;

}
