package com.transfer.project.service;

import com.transfer.project.model.ProjectCreateDTO;
import com.transfer.project.model.ProjectInfoData;
import com.transfer.project.model.TB_PJT_BASE_Entity;

import java.util.List;

public interface TransferProjcet {

    public ProjectInfoData createProject(ProjectCreateDTO projectCreateDTO) throws Exception;

    public List<TB_PJT_BASE_Entity> getDataBaseProjectData() throws Exception;
}
