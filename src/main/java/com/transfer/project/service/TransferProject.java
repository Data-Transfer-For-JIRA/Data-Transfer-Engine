package com.transfer.project.service;

import com.transfer.project.model.dto.ProjectCreateDTO;
import com.transfer.project.model.dto.ProjectInfoData;
import com.transfer.project.model.entity.TB_JML_Entity;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface TransferProject {

    public ProjectInfoData createProject(ProjectCreateDTO projectCreateDTO) throws Exception;

    public Page<TB_PJT_BASE_Entity> getDataBaseProjectData(int pageIndex, int pageSize) throws Exception;

    public Page<TB_PJT_BASE_Entity> getDataBeforeProjectData(int pageIndex, int pageSize) throws Exception;
    public Page<TB_PJT_BASE_Entity> getDataBeforeSeachProjectData(String seachKeyWord,int pageIndex, int pageSize) throws Exception;

    public Page<TB_JML_Entity>  getDataAfterProjectData(int pageIndex, int pageSize) throws Exception;
    public Page<TB_JML_Entity>  getDataAfterSeachProjectData(String seachKeyWord, int pageIndex, int pageSize) throws Exception;

    public Map<String, String> CreateProjectFromDB(int personalId,String projectCode) throws Exception;

    public Boolean checkValidationJiraKey(String key) throws Exception;

}
