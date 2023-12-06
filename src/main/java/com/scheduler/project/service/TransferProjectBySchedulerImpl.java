package com.scheduler.project.service;

import com.account.service.Account;
import com.transfer.project.model.dto.CreateBulkResultDTO;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;
import com.transfer.project.service.TransferProject;
import com.utils.SaveLog;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@Service("transferProjectByScheduler")
public class TransferProjectBySchedulerImpl implements TransferProjectByScheduler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private com.transfer.project.model.dao.TB_PJT_BASE_JpaRepository TB_PJT_BASE_JpaRepository;

    @Autowired
    private TransferProject transferProject;

    @Autowired
    private Account account;
    public void createProject() throws Exception{
        String scheduler_result = null;
        // 프로젝트 10개 조회
        Pageable pageable = PageRequest.of(0, 5);
        Page<TB_PJT_BASE_Entity> page = TB_PJT_BASE_JpaRepository.findAllByMigrateFlagFalseOrderByCreatedDateDesc(pageable);
        // 조회 대상 프로젝트 생성 및 결과 리턴

        for(TB_PJT_BASE_Entity project : page){
            String projectCode = project.getProjectCode();
            Map<String, String> create_result = transferProject.CreateProjectFromDB(1, projectCode);

            // 이관 실패인 경우
            if (create_result.containsKey("이관 실패") && create_result.get("이관 실패").equals(projectCode)) {
                scheduler_result = "["+projectCode+"] 해당 프로젝트 생성에 실패하였습니다.";
            }

            // 프로젝트 조회 실패인 경우
            if (create_result.containsKey("프로젝트 조회 실패") && create_result.get("프로젝트 조회 실패").equals(projectCode)) {
                scheduler_result ="["+projectCode+"] 해당 프로젝트 조회에 실패하였습니다.";
            }

            // 이관 성공인 경우
            if (create_result.containsKey("이관 성공") && create_result.get("이관 성공").equals(projectCode)) {
                scheduler_result = "["+projectCode+"] 해당 프로젝트 생성에 성공하였습니다.";
            }

            // 이미 이관한 프로젝트인 경우
            if (create_result.containsKey("이미 이관한 프로젝트") && create_result.get("이미 이관한 프로젝트").equals(projectCode)) {
                scheduler_result = "["+projectCode+"] 해당 프로젝트는 이미 이관한 프로젝트 입니다.";
            }

            Date currentTime = new Date();

            SaveLog.projectSchedulerResult(scheduler_result,currentTime);

        }

    }
}
