package com.scheduler.issue.service;

import com.account.service.Account;
import com.transfer.issue.model.dto.TransferIssueDTO;
import com.transfer.issue.service.TransferIssue;
import com.transfer.project.model.dao.TB_PJT_BASE_JpaRepository;
import com.transfer.project.model.entity.TB_JML_Entity;
import com.utils.SaveLog;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
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

    @Autowired
    TransferIssue transferIssue;

    @Override
    @Transactional
    public void createIssueByScheduler() throws Exception{
        //오늘 날짜로 생성된 프로젝트 조회
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);
        List<TB_JML_Entity> todayMigratedProjectCodeList= TB_JML_JpaRepository.findProjectCodeByMigratedDateBetween(startOfDay,endOfDay);

        for(TB_JML_Entity todayMigratedProject : todayMigratedProjectCodeList){
            String projectCodeFromJML = todayMigratedProject.getProjectCode();
            boolean isMigrate = TB_PJT_BASE_JpaRepository.findIssueMigrateFlagByProjectCode(projectCodeFromJML);
            System.out.println("@@@@@@@@@@@@@@@@"+isMigrate);
            if(!isMigrate ){ // 이관 플래그가 0이면
                String projectCode = todayMigratedProject.getProjectCode();
                TransferIssueDTO transferIssueDTO = new TransferIssueDTO();
                transferIssueDTO.setProjectCode(projectCode);
                transferIssue.transferIssueData(transferIssueDTO);

                TB_JML_Entity migrateIssue =TB_JML_JpaRepository.findByProjectCode(projectCode);

                String key = migrateIssue.getKey();
                String name = migrateIssue.getJiraProjectName();

                String scheduler_result = "["+projectCode+"] 해당 프로젝트 이슈 생성에 성공하였습니다."+ System.lineSeparator()
                        +"[INFO]"+ System.lineSeparator()
                        +"지라 프로젝트 키: "+key+""+System.lineSeparator()
                        +"지라 프로젝트 이름: "+name+"";

                Date currentTime = new Date();
                // 스케줄러 결과 저장
                SaveLog.SchedulerResult("ISSUE",scheduler_result,currentTime);
            }
        }
    }
}
