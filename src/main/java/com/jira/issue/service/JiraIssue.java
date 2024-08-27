package com.jira.issue.service;

import com.jira.issue.model.dto.TransferIssueDTO;
import com.jira.issue.model.dto.search.*;
import com.jira.issue.model.dto.weblink.RequestWeblinkDTO;
import com.jira.issue.model.dto.weblink.SearchWebLinkDTO;
import com.jira.project.model.entity.TB_JML_Entity;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public interface JiraIssue {
    /*
    *  이슈 조회 API
    * */
    SearchRenderedIssue 이슈_조회(String 이슈_키) throws Exception;
    /*
    * 오늘 생성 및 업데이트 된 이슈 데이터
    * */
    오늘_생성및_업데이트된_이슈데이터 오늘_업데이트및_생성된이슈들() throws Exception;
    /*
     * 해당 프로젝트에 이슈 생성 API - WSS 지라시 사용
     * */
    Map<String ,String> transferIssueData(TransferIssueDTO transferIssueDTO) throws Exception;

    /*
    *  기본정보 이슈 업데이트시 사용 API - 현재 미사용
    * */
    Map<String, String> updateIssueData(TransferIssueDTO transferIssueDTO) throws Exception;

    /*
     *  유지보수_기본정보 이슈 조회 API
     * */
    SearchIssueDTO<SearchMaintenanceInfoDTO> getMaintenanceIssue(String issueKey) throws Exception;
    /*
     *  프로젝트_기본정보 이슈 조회 API
     * */
    SearchIssueDTO<SearchProjectInfoDTO> getProjectIssue(String issueKey) throws Exception;
    /*
    *  프로젝트에 걸링 웹링크 API
    * */
    List<SearchWebLinkDTO> getWebLinkByJiraIssueKey(String jiraKey) throws Exception;
    /*
    *  프로젝트에 생성된 이슈 조회
    * */
    프로젝트에_생성된_이슈데이터 프로젝트에_생성된_이슈조회(String 지라프로젝트_키, String 검색_시작_지점, String 검색_최대_개수) throws Exception;

    /*---------------------------------------------------------------------------------*/

    /*
     *  생성한 이슈의 상태를 변환하는 메서드
     * */
    void changeIssueStatus(String issueKey) throws Exception;
    /*
    *
    * */
    String getOneAssigneeId(String userName) throws Exception;
    /*
    *
    * */
    String getBaseIssueKey(String jiraProjectCode, String issueType);
    /*
     *
     * */
    String getBaseIssueKeyByJiraKey(String jiraKey);
    /*
     *
     * */
    Specification<TB_JML_Entity> hasDateTimeBeforeIsNull(String field);
    /*
     *  프로젝트에 걸린 웹링크 조회
     */
    List<SearchWebLinkDTO> getWebLinkByJiraKey(String jiraKey) throws Exception;

    /*
     *  지라 프로젝트 대상이슈에키와 프로젝트 키로 걸기
     * */
    String createWebLink(RequestWeblinkDTO requestWeblinkDTO) throws Exception;
    /*
     *
     * */
    void setMigrateIssueFlag(String projectId, String projectCode);
    /*
     *
     * */
    Boolean addMentionAndComment(String issueIdOrKey, String targetUser ,String contents) throws Exception;
}
