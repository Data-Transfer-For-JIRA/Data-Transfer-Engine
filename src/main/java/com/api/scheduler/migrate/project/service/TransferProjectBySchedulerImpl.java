package com.api.scheduler.migrate.project.service;

import com.jira.issue.service.TransferIssue;
import com.jira.project.model.dao.TB_JML_JpaRepository;
import com.jira.project.model.dao.TB_PJT_BASE_JpaRepository;
import com.jira.project.model.dto.ProjectDTO;
import com.jira.project.model.entity.TB_JML_Entity;
import com.jira.project.model.entity.TB_PJT_BASE_Entity;
import com.jira.project.service.TransferProject;
import com.utils.SaveLog;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@AllArgsConstructor
@Service("transferProjectByScheduler")
public class TransferProjectBySchedulerImpl implements TransferProjectByScheduler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TB_PJT_BASE_JpaRepository TB_PJT_BASE_JpaRepository;

    @Autowired
    private TB_JML_JpaRepository TB_JML_JpaRepository;

    @Autowired
    private TransferProject transferProject;

    @Autowired
    private TransferIssue transferIssue;

    public void createProject(int project_count) throws Exception{
        String scheduler_resul_fail = null;
        String scheduler_result_success = null;
        // 프로젝트 n개 조회
        Pageable pageable = PageRequest.of(0, project_count);
        Page<TB_PJT_BASE_Entity> page = TB_PJT_BASE_JpaRepository.findAllByMigrateFlagFalseOrderByCreatedDateDesc(pageable);
        // 조회 대상 프로젝트 생성 및 결과 리턴
        for(TB_PJT_BASE_Entity project : page){
            String projectCode = project.getProjectCode();
            Map<String, String> create_result = transferProject.CreateProjectFromDB(1, projectCode);

            // 이관 실패인 경우
            if (create_result.containsKey("이관 실패") && create_result.get("이관 실패").equals(projectCode)) {
                scheduler_resul_fail = "["+projectCode+"] 해당 프로젝트 생성에 실패하였습니다.";
            }

            // 프로젝트 조회 실패인 경우
            if (create_result.containsKey("프로젝트 조회 실패") && create_result.get("프로젝트 조회 실패").equals(projectCode)) {
                scheduler_resul_fail ="["+projectCode+"] 해당 프로젝트 조회에 실패하였습니다.";
            }

            // 이관 성공인 경우
            if (create_result.containsKey("이관 성공") && create_result.get("이관 성공").equals(projectCode)) {

                String key = TB_JML_JpaRepository.findByProjectCode(projectCode).getKey();
                String name = TB_JML_JpaRepository.findByProjectCode(projectCode).getJiraProjectName();
                scheduler_result_success = "["+projectCode+"] 해당 프로젝트 생성에 성공하였습니다."+ System.lineSeparator()
                        +"[INFO]"+ System.lineSeparator()
                        +"생성된 지라 프로젝트 키: "+key+""+System.lineSeparator()
                        +"생성된 지라 프로젝트 이름: "+name+"";

            }

            // 이미 이관한 프로젝트인 경우
            if (create_result.containsKey("이미 이관한 프로젝트") && create_result.get("이미 이관한 프로젝트").equals(projectCode)) {
                scheduler_resul_fail = "["+projectCode+"] 해당 프로젝트는 이미 이관한 프로젝트 입니다.";
            }

            Date currentTime = new Date();
            // 스케줄러 결과 저장
            if(scheduler_resul_fail != null){
                SaveLog.SchedulerResult("PROJECT\\FAIL",scheduler_resul_fail,currentTime);
            }
            SaveLog.SchedulerResult("PROJECT\\SUCCESS",scheduler_result_success,currentTime);
        }
    }


    @Override
    public void reAssgineProjectByScheduler() throws Exception{
        List<TB_JML_Entity> jiraProjectList = TB_JML_JpaRepository.findAll();
        String scheduler_resul_fail = null;
        String scheduler_result_success = null;

        for(TB_JML_Entity project : jiraProjectList){
            Date currentTime = new Date();
            String jiraProjectCode = project.getKey();
            String assignee = project.getProjectAssignees();
            String assigneeId = transferIssue.getOneAssigneeId(assignee);

            ProjectDTO result = transferProject.reassignProjectLeader(jiraProjectCode,assigneeId);

            if(result.getId() != null){
                String leader = result.getLead().getDisplayName();
                scheduler_result_success =  "["+jiraProjectCode+"] 해당 프로젝트의 할당자는 "+leader+"로 재 할당되었습니다.";

                SaveLog.SchedulerResult("ASSIGNEE\\SUCCESS",scheduler_result_success,currentTime);
            }else {
                scheduler_resul_fail = "["+jiraProjectCode+"] 해당 프로젝트는 재 할당에 실패하였습니다.";
                SaveLog.SchedulerResult("ASSIGNEE\\FAIL",scheduler_resul_fail,currentTime);
            }

        }

    }

    /*
    *  오늘 만든 프로젝트 담당자 제할당 api
    * */
    @Override
    public void reAssgineProjectBySchedulerPeriodically() throws Exception{

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);
        List<TB_JML_Entity> jiraProjectList= TB_JML_JpaRepository.findProjectCodeByMigratedDateBetween(startOfDay,endOfDay);
        String scheduler_resul_fail = null;
        String scheduler_result_success = null;

        for(TB_JML_Entity project : jiraProjectList){
            Date currentTime = new Date();
            String jiraProjectCode = project.getKey();
            String assignee = project.getProjectAssignees();
            String assigneeId = transferIssue.getOneAssigneeId(assignee);

            ProjectDTO result = transferProject.reassignProjectLeader(jiraProjectCode,assigneeId);

            if(result.getId() != null){
                String leader = result.getLead().getDisplayName();
                scheduler_result_success =  "["+jiraProjectCode+"] 해당 프로젝트의 할당자는 "+leader+"로 재 할당되었습니다.";

                SaveLog.SchedulerResult("ASSIGNEE\\SUCCESS",scheduler_result_success,currentTime);
            }else {
                scheduler_resul_fail = "["+jiraProjectCode+"] 해당 프로젝트는 재 할당에 실패하였습니다.";
                SaveLog.SchedulerResult("ASSIGNEE\\FAIL",scheduler_resul_fail,currentTime);
            }

        }

    }
}
