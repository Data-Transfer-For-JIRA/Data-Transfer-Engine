package com.transfer.issuetype.service;

import com.transfer.issuetype.model.dto.IssueTypeConnectDTO;

public interface TransferIssueType {

    /* 프로젝트 생성할 때 마다 연결 처리
     * 1. 프로젝트 생성 완료처리
     * 2. 생성한 프로젝트 대상으로 이슈타입 연결작업 시작
     * 3. 프로젝트 생성 후 바로 이슈 타입 연결을 진행 필요 => 따라서 프로젝트 생성시 호출하여 이슈 타입 연결
     * */
    public void setIssueType(String projectId) throws Exception;


    /* 프로젝트 모두 생성 후 이슈 타입 연결할 때
     * 1. 프로젝트 생성 완료처리
     * 2. 생성한 프로젝트 대상으로 이슈타입 연결작업 시작
     * 3. 프로젝트 생성 후 바로 이슈 타입 연결을 진행 필요 => 따라서 프로젝트 생성시 호출하여 이슈 타입 연결
     * */
    public void setIssueTypeScheduler()throws Exception;
}
