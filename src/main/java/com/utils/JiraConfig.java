package com.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Configuration
public class JiraConfig {

    @Value("${jira.info.token}")
    public String apiToken;

    @Value("${jira.info.id}")
    public String jiraID;

    @Value("${jira.info.baseurl}")
    public String baseUrl;



}
