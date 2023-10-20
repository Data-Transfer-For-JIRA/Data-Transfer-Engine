package com.transfer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JiraConfig {

    @Value("${jira.info}")
    public String apitoken;
}
