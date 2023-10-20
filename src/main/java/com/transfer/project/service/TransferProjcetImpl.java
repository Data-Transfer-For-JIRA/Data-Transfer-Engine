package com.transfer.project.service;

import com.transfer.config.JiraConfig;
import com.transfer.project.model.ProjectData;
import com.transfer.project.model.ProjectInfo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service("transferProjcet")
public class TransferProjcetImpl implements TransferProjcet{

    @Autowired
    @Qualifier("JiraConfig")
    private JiraConfig jiraConfig;

    @Override
    public ProjectData createProject(ProjectInfo projectInfo) throws Exception{

        return null;
    }
}
