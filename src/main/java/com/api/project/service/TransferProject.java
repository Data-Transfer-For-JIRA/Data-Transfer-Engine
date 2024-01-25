package com.api.project.service;

import com.api.project.model.dto.CreateProjectDTO;
import com.api.project.model.dto.CreateProjectResponseDTO;
import com.api.project.model.dto.ProjectDTO;
import com.api.project.model.entity.TB_JLL_Entity;
import com.api.project.model.entity.TB_JML_Entity;
import com.api.project.model.entity.TB_PJT_BASE_Entity;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface TransferProject {

    public List<TB_JML_Entity> getJiraProjectListBySearchKeywordOnJML( String searchKeyWord) throws Exception;

    public Map<String, String> CreateProjectFromDB(int personalId,String projectCode) throws Exception;

    public Boolean checkValidationJiraKey(String key) throws Exception;

    public ProjectDTO reassignProjectLeader(String jiraProjectCode, String assignee) throws Exception;

    public ProjectDTO getJiraProjectInfoByJiraKey(String jiraKey) throws Exception;

    List<Map<String, String>> deleteJiraProject(List<String> jiraProjectCodes);
    List<TB_JLL_Entity> saveProjectsRelation() throws Exception;

}
