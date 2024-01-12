package com.transfer.issue.service;

import com.transfer.issue.model.dto.CommentDTO;
import com.transfer.issue.model.dto.TransferIssueDTO;
import com.transfer.issue.model.dto.weblink.RequestWeblinkDTO;
import com.transfer.issue.model.dto.weblink.SearchWebLinkDTO;
import com.transfer.project.model.entity.TB_JML_Entity;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public interface TransferIssue {

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
    /*
    *  지라 프로젝트 대상이슈에키와 프로젝트 키로 걸기
    * */
    String createWebLink(RequestWeblinkDTO requestWeblinkDTO) throws Exception;


    void deleteComment(String issueIdOrKey,String id) throws Exception;

    CommentDTO getComment(String issueIdOrKey) throws Exception;

}
