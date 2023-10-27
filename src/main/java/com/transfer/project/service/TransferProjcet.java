package com.transfer.project.service;

import com.transfer.project.model.ProjectCreateDTO;
import com.transfer.project.model.ProjectInfoData;
import com.transfer.project.model.TB_PJT_BASE_Entity;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TransferProjcet {

    public ProjectInfoData createProject(ProjectCreateDTO projectCreateDTO) throws Exception;

    public Page<TB_PJT_BASE_Entity> getDataBaseProjectData(int pageIndex, int pageSize) throws Exception;
}
