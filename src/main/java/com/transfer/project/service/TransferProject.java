package com.transfer.project.service;

import com.transfer.project.model.dto.CreateProjectDTO;
import com.transfer.project.model.dto.CreateProjectResponseDTO;
import com.transfer.project.model.dto.ProjectDTO;
import com.transfer.project.model.entity.TB_JLL_Entity;
import com.transfer.project.model.entity.TB_JML_Entity;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface TransferProject {

    public CreateProjectResponseDTO createProject(CreateProjectDTO createProjectDTO) throws Exception;

    public Page<TB_PJT_BASE_Entity> getDataBaseProjectData(int pageIndex, int pageSize) throws Exception;

    public Page<TB_PJT_BASE_Entity> getDataBeforeProjectData(int pageIndex, int pageSize) throws Exception;
    public Page<TB_PJT_BASE_Entity> getDataBeforeSeachProjectData(String seachKeyWord,int pageIndex, int pageSize) throws Exception;

    public Page<TB_JML_Entity>  getDataAfterProjectData(int pageIndex, int pageSize) throws Exception;

    public Page<TB_PJT_BASE_Entity>  getTransferredProjectsList(int pageIndex, int pageSize) throws Exception;

    public Page<TB_JML_Entity>  getDataAfterSearchProjectData(String seachKeyWord, int pageIndex, int pageSize) throws Exception;

    public Map<String, String> CreateProjectFromDB(int personalId,String projectCode) throws Exception;

    public Boolean checkValidationJiraKey(String key) throws Exception;

    public ProjectDTO reassignProjectLeader(String jiraProjectCode, String assignee) throws Exception;

    public ProjectDTO getJiraProjectInfoByJiraKey(String jiraKey) throws Exception;

    List<Map<String, String>> deleteJiraProject(List<String> jiraProjectCodes);
    List<TB_JLL_Entity> saveProjectsRelation() throws Exception;

}
