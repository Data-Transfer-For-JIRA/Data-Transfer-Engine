package com.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class 전자문서직원 {
    @Value("${전자문서사업부.직원}")
    public List<String> 직원;
}
