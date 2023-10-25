package com.transfer.project.service;

import com.transfer.project.model.ProjectCreateDTO;
import com.transfer.project.model.ProjectInfoData;

public interface TransferProjcet {

    public ProjectCreateDTO createProject(ProjectCreateDTO projectCreateDTO) throws Exception;
}
