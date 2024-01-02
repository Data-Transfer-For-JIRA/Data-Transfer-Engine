package com.transfer.issue.service;

import com.transfer.issue.model.dto.CreateIssueDTO;
import com.transfer.issue.model.dto.TransferIssueDTO;

import java.util.Map;

public interface TransferIssue {

    Map<String ,String> transferIssueData(TransferIssueDTO transferIssueDTO) throws Exception;

    /*
     *  생성한 이슈의 상태를 변환하는 메서드
     * */
    void changeIssueStatus(String issueKey) throws Exception;
}
