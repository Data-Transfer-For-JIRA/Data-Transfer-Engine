package com.transfer.issue.service;


import com.account.dto.AdminInfoDTO;
import com.account.service.Account;
import com.transfer.issue.model.dao.PJ_PG_SUB_JpaRepository;
import com.transfer.issue.model.dto.CreateBulkIssueDTO;
import com.transfer.issue.model.dto.CreateIssueDTO;
import com.transfer.issue.model.dto.FieldDTO;
import com.transfer.issue.model.entity.PJ_PG_SUB_Entity;
import com.transfer.project.model.entity.TB_JML_Entity;
import com.utils.WebClientUtils;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

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
    public Map<String ,String> transferIssueData(String projectCode) throws Exception {
        logger.info("이슈 생성 시작");
        Map<String, String> result = new HashMap<>();

        // 생성할 프로젝트 조회
        TB_JML_Entity project = checkProjectCreated(projectCode);
        // 지라 프로젝트 키
        String jiraProjectKey  = project.getKey();
        // 지라 프로젝트 아이디
        Integer jiraProjectId = project.getId();
        // 프로젝트 담당자 조회
        String projectAssignees = project.getProjectAssignees();

        if(project == null){
            logger.info("생성된 프로젝트가 아닙니다.");
            result.put("해당 프로젝트는 지라에없습니다.",projectCode);
        }else{
            // WSS 데이터 조회
            logger.info("이슈생성을 시작합니다.");
            // 이슈 조회
            List<PJ_PG_SUB_Entity> issueList= PJ_PG_SUB_JpaRepository.findAllByProjectCodeOrderByCreationDateAsc(projectCode);
            
            if(createFirstIssue(issueList,jiraProjectKey,projectAssignees)){
                logger.info("최초 이슈 생성 성공");
                createBulkIssue(issueList,jiraProjectId);
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


    public Boolean createFirstIssue(List<PJ_PG_SUB_Entity> issueList, String jiraProjectKey , String projectAssignees) throws Exception {
        logger.info("최초 이슈 생성 시도");

        PJ_PG_SUB_Entity firstIssue = issueList.get(0); // 이슈 최초 컨텐츠
        String issueContent = firstIssue.getIssueContent(); // 이슈 내용
        Date creationDate   = firstIssue.getCreationDate(); // 이슈 생성일
        List<String> assignees = returnProjectAssigneeId(projectAssignees); // 프로젝트 담당자 리스트
        for (String assignee : assignees) {
            System.out.println("[::TransferIssueImpl::] assignee -> " + assignee);
        }

        String replaced_string = issueContent.replace("<br>", "\n").replace("&nbsp;", "    ");

         String plainText = Jsoup.clean(issueContent, Whitelist.none());



        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());

        return true;
    }

    public String createBulkIssue(List<PJ_PG_SUB_Entity> issueList , Integer jiraProjectId) throws Exception {

        logger.info("나머지 이슈 생성");
        List<PJ_PG_SUB_Entity> nomalIssueList = issueList.subList(1, issueList.size());

        CreateBulkIssueDTO bulkIssueDTO = new CreateBulkIssueDTO();

        List<FieldDTO> issueUpdates = new ArrayList<>();

        for(PJ_PG_SUB_Entity issueData : nomalIssueList){

            String wssAssignee = returnIssueAssigneeId(issueData.getWriter());
            String wssContent  = issueData.getIssueContent();
            Date wssWriteDate  = issueData.getCreationDate();



            FieldDTO fieldDTO = new FieldDTO();
            // 담당자
            fieldDTO.getAssignee().setAccountId(String.valueOf(wssAssignee));
            // 프로젝트 아이디
            fieldDTO.getProject().setId(jiraProjectId);
            // wss 이슈 제목
            String summary = "["+wssWriteDate+"] WSS 작성이슈";
            fieldDTO.setSummary(summary);
            // wss 이슈
            fieldDTO.getDescription().setType("doc");
            fieldDTO.getDescription().setVersion(1);

            FieldDTO.Content content = new FieldDTO.Content();
            content.setType("paragraph");
            FieldDTO.ContentItem contentItem = new FieldDTO.ContentItem();
            contentItem.setText(wssContent);
            contentItem.setType("text");
            content.setContent((List<FieldDTO.ContentItem>) contentItem);
            fieldDTO.getDescription().setContent((List<FieldDTO.Content>) content);


            issueUpdates.add(fieldDTO);

        }
        bulkIssueDTO.setIssueUpdates(issueUpdates);


        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint ="/rest/api/3/issue/bulk";
        WebClientUtils.post(webClient,endpoint,issueList,String.class);
        return "1";
    }

    public List<String> returnProjectAssigneeId(String userNames)throws Exception{
        logger.info("프로젝트 담당자 이름 아이디 변환");

        if(userNames != null && !userNames.trim().isEmpty()){
            String[] namesArray = userNames.trim().split("\\s*,\\s*");

            List<String> namesArrayList = Arrays.asList(namesArray);

            List<String> userIdList = new ArrayList<>();

            for (String name : namesArrayList) {

                String userId = TB_JIRA_USER_JpaRepository.findByDisplayNameContaining(name).getAccountId();

                userIdList.add(userId);
            }
            // 앞에 2개의 데이터만 추출하여 반환
            return userIdList.subList(0, Math.min(userIdList.size(), 2));
        }else {
            // 담당자 미지정된 프로젝트 (전자문서사업부 아이디)
            return null;
        }
    }

    public String returnIssueAssigneeId(String userName) throws Exception{
        logger.info("이슈 생성자 아이디 변환 및 조회");

        if( userName != null && userName.isEmpty()){

            String userId = TB_JIRA_USER_JpaRepository.findByDisplayNameContaining(userName).getAccountId();

            return userId;
        }else {
            return null;
        }

    }



}
