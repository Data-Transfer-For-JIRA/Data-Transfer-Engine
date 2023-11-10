package com.utils;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ProjectConfig {

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

    @Value("${issueTypeSchemeId.default}")
    public  String issuetypeId;

    @Value("${issueTypeScreenScheme.project}")
    public  String projectIssueType;

    @Value("${issueTypeScreenScheme.maintenance}")
    public  String maintenanceIssueType;


}