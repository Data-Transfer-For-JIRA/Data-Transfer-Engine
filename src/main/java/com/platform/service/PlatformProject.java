package com.platform.service;

import com.platform.dto.BaseDTO;

import java.util.Map;

public interface PlatformProject {

    Map<String, String> platformCreateProject(BaseDTO baseDTO) throws Exception;

    Map<String, String> platformCreateProject(String jiraProjectCode, String projectFlag, String projectName, String projectCode, String assignees) throws Exception;

    Map<String, String> platformService(BaseDTO baseDTO) throws Exception;
}
