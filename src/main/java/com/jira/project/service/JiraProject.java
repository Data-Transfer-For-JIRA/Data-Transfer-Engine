package com.jira.project.service;

import com.jira.project.model.DeleteProject;
import com.jira.project.model.dto.CreateProjectDTO;
import com.jira.project.model.dto.ProjectDTO;
import com.jira.project.model.entity.TB_JLL_Entity;
import com.jira.project.model.entity.TB_JML_Entity;

import java.util.List;
import java.util.Map;

public interface JiraProject {
    /*
    *  JML 테이블에서 키워드를 이용해 지라키, 프로젝트 코드, 프로젝트 이름 검색 기능
    * */
    public List<TB_JML_Entity> getJiraProjectListBySearchKeywordOnJML(String searchKeyWord) throws Exception;

    /*
    *  WSS 프로젝트 코드를 이용해 TB_PJT_BASE 테이블의 정보를 조회하여 지라 프로젝트를 만드는 기능
    * */
    public Map<String, String> createProjectFromDB( String projectCode) throws Exception;

    /*
    *  만들고자 하는 지라키가 합당한지 판별하는 기능 (JML 테이블 및 지라 서버 조회)
    * */
    public Boolean checkValidationJiraKey(String key) throws Exception;

    /*
    * 지라 프로젝트 코드와 담당자 이름으로 지라 프로젝트의 담당자를 변경하는 기능
    * */
    public ProjectDTO reassignProjectLeader(String jiraProjectCode, String assignee) throws Exception;

    /*
    * 지라 프로젝트를 조회하는 기능
    * */
    public ProjectDTO getJiraProjectInfoByJiraKey(String jiraKey) throws Exception;

    /*
    *  지라 프로젝트키 리스트를 이용해 지라 프로젝트 삭제하는 기능(테스트로 생성한 지라 프로젝트 사용하기 위한 용도)
    * */
    List<Map<String, String>> deleteJiraProject(DeleteProject deleteProject, List<String> jiraProjectCodes);

    /*
    *  TB_PJT_BASE 테이블에서 WSS의 프로젝트 관계를 디비에 저장하는 기능(1회 수행 하는 기능)
    * */
    List<TB_JLL_Entity> saveProjectsRelation() throws Exception;

    ProjectDTO  updateProjectInfo(CreateProjectDTO createProjectDTO) throws Exception;

    List<String> getJiraProject() throws Exception;

}
