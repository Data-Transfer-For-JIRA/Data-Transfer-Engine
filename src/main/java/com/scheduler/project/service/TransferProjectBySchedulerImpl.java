package com.scheduler.project.service;

import com.account.service.Account;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service("transferProjectByScheduler")
public class TransferProjectBySchedulerImpl implements TransferProjectByScheduler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Account account;
    public void createProject() throws Exception{
        // 프로젝트 10개 조회

        // 조회 대상 프로젝트 생성

        // 결과 값 리턴

        // 해당 결과 로그백 으로 저장

    }
}
