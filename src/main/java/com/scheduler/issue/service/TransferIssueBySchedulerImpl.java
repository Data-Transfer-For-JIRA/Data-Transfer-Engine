package com.scheduler.issue.service;

import com.account.service.Account;
import com.transfer.project.model.dao.TB_PJT_BASE_JpaRepository;
import com.transfer.project.model.entity.TB_JML_Entity;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service("TransferIssueByScheduler")
public class TransferIssueBySchedulerImpl implements TransferIssueByScheduler{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Account account;

    @Autowired
    private com.transfer.project.model.dao.TB_JML_JpaRepository TB_JML_JpaRepository;

    @Autowired
    private com.transfer.project.model.dao.TB_PJT_BASE_JpaRepository TB_PJT_BASE_JpaRepository;

    @Override
    public void createIssueByScheduler() throws Exception{
        //오늘 날짜로 생성된 프로젝트 조회
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);
        List<String> todayMigratedProjectCodeList= TB_JML_JpaRepository.findProjectCodeByMigratedDateBetween(startOfDay,endOfDay);
        // 프로젝트 이관 플레그 확인

        List<String> projectBeforeIssueTransfer = new ArrayList<>();

        for(String todayMigratedProjectCode : todayMigratedProjectCodeList){

            if(TB_PJT_BASE_JpaRepository.findIssueMigrateFlagByProjectCode(todayMigratedProjectCode) ){

            }else{

            }

        }

        // 이슈 생성 안되어있으면 이슈 생성

        // 되어있으면 리턴
    }
}
