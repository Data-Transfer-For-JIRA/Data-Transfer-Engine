package com.transfer.issue.service;


import com.account.dto.AdminInfoDTO;
import com.account.service.Account;
import com.transfer.issue.model.dao.PJ_PG_SUB_JpaRepository;
import com.transfer.issue.model.dto.CreateIssueDTO;
import com.transfer.issue.model.dto.FieldDTO;
import com.transfer.issue.model.entity.PJ_PG_SUB_Entity;
import com.transfer.project.model.entity.TB_JML_Entity;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Date;
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

    @Autowired
    private com.account.dao.TB_JIRA_USER_JpaRepository TB_JIRA_USER_JpaRepository;
    @Transactional
    @Override
    public Map<String ,String> transferIssueData(CreateIssueDTO createIssueDTO) throws Exception {
        logger.info("이슈 생성 시작");
        Map<String, String> result = new HashMap<>();

        String projectCode = createIssueDTO.getProjectCode();

        // 생성할 프로젝트 조회
        TB_JML_Entity project = checkProjectCreated(projectCode);
        // 지라 프로젝트 키
        String jiraProjectKey  = project.getKey();
        
        if(project == null){
            logger.info("생성된 프로젝트가 아닙니다.");
            result.put("해당 프로젝트는 지라에없습니다.",projectCode);
        }else{
            // WSS 데이터 조회
            logger.info("이슈생성을 시작합니다.");
            // 이슈 조회
            List<PJ_PG_SUB_Entity> issueList= PJ_PG_SUB_JpaRepository.findAllByProjectCodeOrderByCreationDateAsc(projectCode);

            // 조회 대상 지라 유저 아이디
            String jiraUserId = TB_JIRA_USER_JpaRepository.findByDisplayNameContaining(issueList.get(0).getWriter()).getAccountId();
            
            if(createFirstIssue(issueList,jiraProjectKey, jiraUserId)){
                logger.info("최초 이슈 생성 성공");
                createBulkIssue(issueList,jiraProjectKey ,jiraUserId);
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


    public Boolean createFirstIssue(List<PJ_PG_SUB_Entity> issueList, String jiraProjectKey, String jiraUserId){
        logger.info("최초 이슈 생성 시도");

        PJ_PG_SUB_Entity first = issueList.get(0);

        String 이슈내용 = first.getIssueContent();
        Date 생성날짜   = first.getCreationDate();
        String plainText = Jsoup.clean(이슈내용, Whitelist.none());

        FieldDTO fieldDTO            = new FieldDTO();
        FieldDTO.Project   project   = new FieldDTO.Project();
        FieldDTO.Assignee  assignee  = new FieldDTO.Assignee();
        FieldDTO.Status    status    = new FieldDTO.Status();
        FieldDTO.IssueType issueType = new FieldDTO.IssueType();

        project.setId(jiraProjectKey);
        assignee.setAccountId(jiraUserId);
//        status.setId();
//        issueType.setId();

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());

        return true;
    }

    public String createBulkIssue(List<PJ_PG_SUB_Entity> issueList , String jiraProjectKey, String jiraUserId){
        logger.info("나머지 이슈 생성");

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint ="";
        WebClientUtils.post(webClient,endpoint,issueList,String.class);
        return "1";
    }

}
