package com.transfer.project.service;

import com.transfer.project.model.ProjectData;
import com.transfer.project.model.ProjectInfo;

public interface TransferProjcet {

    public ProjectData createProject( ProjectInfo projectInfo) throws Exception;
}
