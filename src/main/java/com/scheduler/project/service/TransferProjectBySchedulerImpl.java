package com.scheduler.project.service;

import com.account.service.Account;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;
import com.transfer.project.service.TransferProject;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // 프로젝트 10개 조회
        Pageable pageable = PageRequest.of(0, 10);
        Page<TB_PJT_BASE_Entity> page = TB_PJT_BASE_JpaRepository.findAllByMigrateFlagFalseOrderByCreatedDateDesc(pageable);
        // 조회 대상 프로젝트 생성 및 결과 리턴
        List<Map<String, String>> result_all = new ArrayList<>();
        Map<String, String> result = new HashMap<>();

        for(TB_PJT_BASE_Entity project : page){
            String projectCode = project.getProjectCode();
            result = transferProject.CreateProjectFromDB(1,projectCode);
            result_all.add(result);
        }
        // 해당 결과 로그백 으로 저장

    }
}
