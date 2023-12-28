package com.platform.service;

import com.platform.dto.BaseDTO;

import java.util.Map;

public interface PlatformProject {

    Map<String, String> platformCreateProject(BaseDTO baseDTO) throws Exception;
}
