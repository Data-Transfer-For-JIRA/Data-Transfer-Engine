package com.utils;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ProjectConfig {


    // ======================== 프로젝트 생성을 위한 기본정보 ======================== //
    @Value("${project.create.assigneType}")
    public String assigneType;

    @Value("${project.create.categoryId}")
    public Integer categoryId;

    @Value("${project.create.leadAccountId}")
    public String leadAccountId;

    @Value("${project.create.projectTypeKey}")
    public String projectTypeKey;

    @Value("${project.create.url}")
    public String url;

    // ======================================================================= //
//    @Value("${issueTypeSchemeId.default}")
//    public  String issuetypeId;

    // ======================== 이슈타입 연결을 위한 기본정보 ======================== //
    @Value("${issueTypeSchemeId.project}")
    public  String projectIssueTypeScheme;

    @Value("${issueTypeSchemeId.maintenance}")
    public  String maintenanceIssueTypeScheme;

    @Value("${issueTypeScreenScheme.project}")
    public  String projectIssueTypeScreenScheme;

    @Value("${issueTypeScreenScheme.maintenance}")
    public  String maintenanceIssueTypeScreenScheme;

    @Value("${issueType.project}")
    public String projectIssueType;

    @Value("${issueType.maintenance}")
    public String maintenanceIssueType;
    // ======================================================================= //

    // ======================== 템플릿을 통해 생성을 위한 기본정보 ======================== //
    // 프로젝트 템플릿
    @Value("${template.project}")
    public String projectTemplate;

    @Value("${template.maintenance}")
    public String maintenanceTemplate;
    // ======================================================================= //


}
