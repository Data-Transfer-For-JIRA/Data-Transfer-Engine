package com.transfer.project.service;

import com.transfer.project.model.dto.ProjectCreateDTO;
import com.transfer.project.model.dto.ProjectInfoData;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface TransferProjcet {

    public ProjectInfoData createProject(ProjectCreateDTO projectCreateDTO) throws Exception;

    public Page<TB_PJT_BASE_Entity> getDataBaseProjectData(int pageIndex, int pageSize) throws Exception;

    public Map<String, Boolean> CreateProjectFromDB(String projectCode) throws Exception;
}
