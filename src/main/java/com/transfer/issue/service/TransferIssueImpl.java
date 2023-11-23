package com.transfer.issue.service;


import com.account.dto.AdminInfoDTO;
import com.account.service.Account;
import com.transfer.issue.model.dao.PJ_PG_SUB_JpaRepository;
import com.transfer.issue.model.dto.CreateIssueDTO;
import com.transfer.issue.model.entity.PJ_PG_SUB_Entity;
import com.transfer.project.model.dao.TB_JML_JpaRepository;
import com.transfer.project.model.entity.TB_JML_Entity;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service("transferIssue")
public class TransferIssueImpl implements TransferIssue {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Account account;

    @Autowired
    private com.transfer.project.model.dao.TB_JML_JpaRepository TB_JML_JpaRepository;

    @Autowired
    private PJ_PG_SUB_JpaRepository PJ_PG_SUB_JpaRepository;

    @Override
    public Map<String ,String> transferIssueData(CreateIssueDTO createIssueDTO) throws Exception {
        logger.info("이슈 생성 시작");
        Map<String, String> result = new HashMap<>();

        String projectCode = createIssueDTO.getProjectCode();

        // 생성할 프로젝트 조회
        TB_JML_Entity project = checkProjectCreated(projectCode);
        
        if(project == null){
            logger.info("생성된 프로젝트가 아닙니다.");
            result.put("해당 프로젝트는 지라에없습니다.",projectCode);
        }else{
            // WSS 데이터 조회
            logger.info("이슈생성을 시작합니다.");
            List<PJ_PG_SUB_Entity> issueList= PJ_PG_SUB_JpaRepository.findAllByProjectCodeOrderByCreationDateDesc(projectCode);
            if(createFirstIssue(issueList)){
                logger.info("최초 이슈 생성 성공");
                createBulkIssue(issueList);
            }else{
                result.put("이슈 생성 실패",projectCode);
            }
        }
        return result;
    }
    public TB_JML_Entity checkProjectCreated(String projectCode){
        logger.info("자리에 생성된 프로젝트 조회");
       return TB_JML_JpaRepository.findByProjectCode(projectCode);
    }


    public Boolean createFirstIssue(List<PJ_PG_SUB_Entity> issueList){
        logger.info("최초 이슈 생성 시도");
        

        return true;
    }

    public String createBulkIssue(List<PJ_PG_SUB_Entity> issueList){
        logger.info("나머지 이슈 생성");

        return "1";
    }

}
