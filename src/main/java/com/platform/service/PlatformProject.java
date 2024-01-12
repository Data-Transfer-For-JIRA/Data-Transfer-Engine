package com.platform.service;

import com.platform.dto.BaseDTO;
import com.platform.dto.ReturnMessage;

import java.util.Map;

public interface PlatformProject {

    Map<String, String> platformCreateProject(BaseDTO baseDTO) throws Exception;

    Map<String, String> platformCreateProject(String jiraProjectCode, String projectFlag, String projectName, String projectCode, String assignees) throws Exception;

    Map<String, String> platformService(BaseDTO baseDTO) throws Exception;

    ReturnMessage platformWeblink(String mainJiraKey, String subJiraKey) throws Exception;
}
