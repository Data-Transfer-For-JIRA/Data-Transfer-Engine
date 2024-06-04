package com.jira.issue.service;

import com.jira.issue.model.dto.TransferIssueDTO;
import com.jira.issue.model.dto.search.SearchIssueDTO;
import com.jira.issue.model.dto.search.SearchMaintenanceInfoDTO;
import com.jira.issue.model.dto.search.SearchProjectInfoDTO;
import com.jira.issue.model.dto.weblink.RequestWeblinkDTO;
import com.jira.issue.model.dto.weblink.SearchWebLinkDTO;
import com.jira.project.model.entity.TB_JML_Entity;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public interface JiraIssue {

    Map<String ,String> transferIssueData(TransferIssueDTO transferIssueDTO) throws Exception;

    /*
     *  생성한 이슈의 상태를 변환하는 메서드
     * */
    void changeIssueStatus(String issueKey) throws Exception;
    /**/
    public String getOneAssigneeId(String userName) throws Exception;

    Map<String, String> updateIssueData(TransferIssueDTO transferIssueDTO) throws Exception;

    String getBaseIssueKey(String jiraProjectCode, String issueType);

    String getBaseIssueKeyByJiraKey(String jiraKey);

    Specification<TB_JML_Entity> hasDateTimeBeforeIsNull(String field);

    /*
    *  프로젝트에 걸린 웹링크 조회
    */
    List<SearchWebLinkDTO> getWebLinkByJiraKey(String jiraKey) throws Exception;
    List<SearchWebLinkDTO> getWebLinkByJiraIssueKey(String jiraKey) throws Exception;

    /*
    *  지라 프로젝트 대상이슈에키와 프로젝트 키로 걸기
    * */
    String createWebLink(RequestWeblinkDTO requestWeblinkDTO) throws Exception;

    void setMigrateIssueFlag(String projectId, String projectCode);

    Boolean addComment(String issueIdOrKey, String contents) throws Exception;

    Boolean addMention(String issueIdOrKey, String targetUser) throws Exception;

    Boolean addMentionAndComment(String issueIdOrKey, String targetUser ,String contents) throws Exception;

    /*
    *  유지보수_기본정보 이슈 조회
    * */
    SearchIssueDTO<SearchMaintenanceInfoDTO> getMaintenanceIssue(String issueKey) throws Exception;
    /*
     *  프로젝트_기본정보 이슈 조회
     * */
    SearchIssueDTO<SearchProjectInfoDTO> getProjectIssue(String issueKey) throws Exception;

    Boolean 기본정보이슈_저장(String 지라_키,String 프로젝트_유형) throws Exception;
}
