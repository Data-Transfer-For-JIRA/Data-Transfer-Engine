package com.transfer.project.service;

import com.transfer.project.model.dto.ProjectCreateDTO;
import com.transfer.project.model.dto.ProjectInfoData;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface TransferProjcet {

    public ProjectInfoData createProject(ProjectCreateDTO projectCreateDTO) throws Exception;

    public Page<TB_PJT_BASE_Entity> getDataBaseProjectData(int pageIndex, int pageSize) throws Exception;

    public Page<TB_PJT_BASE_Entity> getDataBeforeProjectData(int pageIndex, int pageSize) throws Exception;
    public List<TB_PJT_BASE_Entity> getDataBeforeSeachProjectData(String seachKeyWord) throws Exception;

    public Page<TB_PJT_BASE_Entity> getDataAfterProjectData(int pageIndex, int pageSize) throws Exception;
    public List<TB_PJT_BASE_Entity> getDataAfterSeachProjectData(String seachKeyWord) throws Exception;

    public Map<String, Boolean> CreateProjectFromDB(int personalId,String projectCode) throws Exception;

}
