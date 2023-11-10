package com.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class 전자문서직원 {
    @Value("${전자문서사업부.사업부장}")
    public String 사업부장;

    @Value("${전자문서사업부.사업개발팀.팀장}")
    public String 사업개발팀_팀장;

    @Value("${전자문서사업부.사업개발팀.팀원}")
    public List<String> 사업개발팀_팀원;

    @Value("${전자문서사업부.PIO1.팀장}")
    public String PIO1_팀장;

    @Value("${전자문서사업부.사업개발팀.팀원}")
    public List<String> PIO1_팀원;


    @Value("${전자문서사업부.PIO2.팀장}")
    public String PIO2_팀장;

    @Value("${전자문서사업부.PIO2.1파트.파트장}")
    public String PIO2_1파트_파트장;

    @Value("${전자문서사업부.PIO2.1파트.팀원}")
    public String PIO2_1파트_팀원;
    @Value("${전자문서사업부.PIO2.2파트.파트장}")
    public String PIO2_2파트_파트장;

    @Value("${전자문서사업부.PIO2.1파트.팀원}")
    public String PIO2_2파트_팀원;
    @Value("${전자문서사업부.PIO2.1파트.파트장}")
    public String PIO2_3파트_파트장;

    @Value("${전자문서사업부.PIO2.1파트.팀원}")
    public String PIO2_3파트_팀원;

    @Value("${전자문서사업부.직원}")
    public List<String> 직원;
}
