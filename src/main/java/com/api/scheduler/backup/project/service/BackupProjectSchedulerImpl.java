package com.api.scheduler.backup.project.service;

import com.jira.project.model.dao.TB_JML_JpaRepository;
import com.jira.project.model.dto.ProjectDTO;
import com.jira.project.service.JiraProject;
import com.utils.SaveLog;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.jira.project.model.entity.TB_JML_Entity;

import java.util.Date;

@AllArgsConstructor
@Service("backupProjectScheduler")
public class BackupProjectSchedulerImpl implements BackupProjectScheduler {

    @Autowired
    JiraProject jiraProject;

    @Autowired
    private TB_JML_JpaRepository TB_JML_JpaRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void updateJMLProjectLeader() throws Exception{
        try {
            int page = 0;
            final int size = 100; // 한 페이지당 항목 수, 조정 가능

            while (true) {
                try {
                    Pageable pageable = PageRequest.of(page, size);
                    Page<TB_JML_Entity> entityPage = TB_JML_JpaRepository.findAll(pageable);

                    if (!entityPage.hasContent()) {
                        break; // 더 이상 처리할 데이터가 없으면 반복 종료
                    }

                    entityPage.forEach(entity -> {
                        try {
                            logger.info(":: 프로젝트 담당자 백업 스케줄러 ::");
                            Date currentTime = new Date();
                            String scheduler_result_success = null;

                            ProjectDTO 프로젝트_조회_데이터 = jiraProject.getJiraProjectInfoByJiraKey(entity.getKey());
                            String 프로젝트_실제_담당자 = 프로젝트_조회_데이터.getLead().getDisplayName(); // api를 통해 조회한 데이토

                            String 기존_저장된_담당자 = entity.getJiraProjectLeader(); // 디비에서 조회한 데이터
                            String 지라_프로젝트키 = 프로젝트_조회_데이터.getKey(); // 지라에서 조회한 프로젝트 키

                            Boolean 담당자_업데이트 = updateProjectLeader(지라_프로젝트키, 기존_저장된_담당자 , 프로젝트_실제_담당자);

                            if(담당자_업데이트){
                                scheduler_result_success =  "["+지라_프로젝트키+"] 해당 프로젝트의 할당자는 "+프로젝트_실제_담당자+"로 재 할당되었습니다.";
                                SaveLog.SchedulerResult("BACKUP\\ASSIGNEE\\SUCCESS",scheduler_result_success,currentTime);
                            }
                        } catch (Exception e) {
                            logger.error(":: 프로젝트 담당자 할당 스케줄러 :: 오류 발생 "+ e.getMessage());
                            throw new RuntimeException(e);
                        }
                    });

                    page++; // 다음 페이지로
                } catch (Exception e) {
                    logger.error(":: 프로젝트 담당자 할당 스케줄러 :: 오류 발생 " + e.getMessage());
                    throw new Exception(e);
                }
            }
        }catch (Exception e){
            logger.error(":: 프로젝트 담당자 할당 스케줄러 :: 오류 발생 "+ e.getMessage());
            throw new Exception(e);
        }
    }

    private Boolean updateProjectLeader(String 지라_프로젝트키,String 기존_저장된_담당자,String 프로젝트_실제_담당자){

        String 담당자_이름;

        if(프로젝트_실제_담당자 == null){
            return false;
        }

        if(프로젝트_실제_담당자.contains("(")){
            int startIndex = 프로젝트_실제_담당자.indexOf("(");
            담당자_이름 = 프로젝트_실제_담당자.substring(0, startIndex).trim(); // 대부분 모든 사람의 이름 뒤에 영어 이름이 붙어 나옴
        }else{
            담당자_이름 = 프로젝트_실제_담당자; // epage dev 케이스
        }

        if(!담당자_이름.equals(기존_저장된_담당자)){ // 디비 데이터와 비교해서 다르면 저장 및 업데이트

            TB_JML_Entity 업데이트_대상_프로젝트 = TB_JML_JpaRepository.findByKey(지라_프로젝트키);

            업데이트_대상_프로젝트.setJiraProjectLeader(담당자_이름);

            TB_JML_JpaRepository.save(업데이트_대상_프로젝트);

            return true;
        }
        return false;
    }
}
