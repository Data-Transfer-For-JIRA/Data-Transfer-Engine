package com.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ProjectConfig {


    // ======================== 템플릿을 통해 생성을 위한 기본정보 ======================== //
    // 프로젝트 템플릿
    @Value("${template.project}")
    public String projectTemplate;

    @Value("${template.maintenance}")
    public String maintenanceTemplate;
    // ======================================================================= //

    @Value("${create.project}")
    public String projectHeaderName;

    @Value("${create.maintenance}")
    public String maintenanceHeaderName;

    @Value("${create.key}")
    public String keyHeader;

    @Value("${create.num}")
    public int projectKeyNum;

}
