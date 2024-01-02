package com.scheduler.issue.service;

import com.account.dto.AdminInfoDTO;
import com.account.entity.TB_JIRA_USER_Entity;
import com.account.service.Account;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scheduler.issue.model.bulk.ResponseBulkIssueDTO;
import com.transfer.issue.model.dto.FieldDTO;
import com.transfer.issue.model.dto.TransferIssueDTO;
import com.scheduler.issue.model.bulk.CreateBulkIssueDTO;
import com.scheduler.issue.model.bulk.CreateBulkIssueFieldsDTO;
import com.transfer.issue.model.entity.PJ_PG_SUB_Entity;
import com.transfer.issue.service.TransferIssue;
import com.transfer.project.model.entity.TB_JML_Entity;
import com.transfer.project.service.TransferProject;
import com.utils.SaveLog;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.*;

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
    private com.account.dao.TB_JIRA_USER_JpaRepository TB_JIRA_USER_JpaRepository;

    @Autowired
    private com.transfer.issue.model.dao.PJ_PG_SUB_JpaRepository PJ_PG_SUB_JpaRepository;

    @Autowired
    TransferProject transferProject;

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
            Date currentTime = new Date();
            String projectCodeFromJML = todayMigratedProject.getProjectCode();
            boolean isMigrate = TB_PJT_BASE_JpaRepository.findIssueMigrateFlagByProjectCode(projectCodeFromJML);
            if(!isMigrate ){ // 이관 플래그가 0이면
                String projectCode = todayMigratedProject.getProjectCode();
                TransferIssueDTO transferIssueDTO = new TransferIssueDTO();
                transferIssueDTO.setProjectCode(projectCode);
                transferIssue.transferIssueData(transferIssueDTO); // 이슈 생성 작업

                boolean isMigrateAfter = TB_PJT_BASE_JpaRepository.findIssueMigrateFlagByProjectCode(projectCodeFromJML);

                if(isMigrateAfter){
                    TB_JML_Entity migrateIssue =TB_JML_JpaRepository.findByProjectCode(projectCode);

                    String key = migrateIssue.getKey();
                    String name = migrateIssue.getJiraProjectName();

                    String scheduler_result_success = "["+projectCode+"] 해당 프로젝트 이슈 생성에 성공하였습니다."+ System.lineSeparator()
                            +"[INFO]"+ System.lineSeparator()
                            +"지라 프로젝트 키: "+key+""+System.lineSeparator()
                            +"지라 프로젝트 이름: "+name+"";
                    // 스케줄러 결과 저장
                    SaveLog.SchedulerResult("ISSUE\\SUCCESS",scheduler_result_success,currentTime);
                }else{
                    String scheduler_result_fail = "["+projectCode+"] 해당 프로젝트 이슈 생성에 실패하였습니다.";
                    SaveLog.SchedulerResult("ISSUE\\FAIL",scheduler_result_fail,currentTime);
                }


            }

        }
    }
    @Override
    @Transactional
    public void transferIssueByDate(Date date) throws Exception{
        logger.info("[::TransferIssueBySchedulerImpl::] transferIssueByDate");
        // 해당 날짜에 생성된 이슈 조회
        List<PJ_PG_SUB_Entity> createdIssueList = PJ_PG_SUB_JpaRepository.findByCreationDate(date);
        // 벌크로 이슈 생성
        createBulkIssue(createdIssueList);
    }
    @Override
    @Transactional
    public void periodicallyCreateIssueByScheduler() throws Exception{
        logger.info("[::TransferIssueBySchedulerImpl::] periodicallyCreateIssueByScheduler");
        // 오늘 날짜-1에 생성된 이슈가 있는지 조회 있으면 벌크로 생성
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date yesterday = cal.getTime();
        // 스케줄러가 돌기 전날에 생성된 이슈 조회
        List<PJ_PG_SUB_Entity> createdIssueList = PJ_PG_SUB_JpaRepository.findByCreationDate(yesterday);
        if(!createdIssueList.isEmpty()){
            // 벌크로 이슈 생성
            createBulkIssue(createdIssueList);
        }
    }
    public ResponseBulkIssueDTO createBulkIssue(List<PJ_PG_SUB_Entity> issueList) throws Exception {
        logger.info("[::TransferIssueBySchedulerImpl::] createBulkIssue");

        CreateBulkIssueDTO bulkIssueDTO = new CreateBulkIssueDTO();

        List<CreateBulkIssueFieldsDTO> issueUpdates = new ArrayList<>();

        for(PJ_PG_SUB_Entity issueData : issueList){

            String wssAssignee = transferIssue.getOneAssigneeId(issueData.getWriter());

            String wssContent  = issueData.getIssueContent();
            Date wssWriteDate  = issueData.getCreationDate();

            String defaultIssueContent = "\n[" + wssWriteDate  + "]\n본 이슈는 WSS에서 이관한 이슈입니다.\n―――――――――――――――――――――――――――――――\n"; // 이슈 생성 시 기본 문구
            String replacedIssueContent = wssContent.replace("<br>", "\n").replace("&nbsp;", " "); // 이슈 내용 전처리
            String basicIssueContent = defaultIssueContent + replacedIssueContent; // 이슈 내용

            FieldDTO fieldDTO = new FieldDTO();
            // 담당자
            FieldDTO.User user = FieldDTO.User.builder()
                    .accountId(wssAssignee).build();
            fieldDTO.setAssignee(user);

            // 프로젝트 아이디
            String wssProjectCode = issueData.getProjectCode();
            String jiraProjectId = TB_JML_JpaRepository.findByProjectCode(wssProjectCode).getId();

            FieldDTO.Project project = FieldDTO.Project.builder()
                    .id(jiraProjectId)
                    .build();
            fieldDTO.setProject(project);

            // wss 이슈 제목
            String summary = "["+wssWriteDate+"] WSS 작성이슈";
            fieldDTO.setSummary(summary);

            // wss 이슈
            FieldDTO.ContentItem contentItem = FieldDTO.ContentItem.builder()
                    .type("text")
                    .text(basicIssueContent)
                    .build();
            List<FieldDTO.ContentItem> contentItems = Collections.singletonList(contentItem);

            FieldDTO.Content content = FieldDTO.Content.builder()
                    .content(contentItems)
                    .type("paragraph")
                    .build();
            List<FieldDTO.Content> contents = Collections.singletonList(content);

            FieldDTO.Description description = FieldDTO.Description.builder()
                    .version(1)
                    .type("doc")
                    .content(contents)
                    .build();
            fieldDTO.setDescription(description);

            FieldDTO.Field field =  FieldDTO.Field.builder()
                    .id("10002")
                    .build();
            fieldDTO.setIssuetype(field);

            CreateBulkIssueFieldsDTO fields = new CreateBulkIssueFieldsDTO();
            fields.setFields(fieldDTO);

            issueUpdates.add(fields);

        }

        bulkIssueDTO.setIssueUpdates(issueUpdates);

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint ="/rest/api/3/issue/bulk";

        ResponseBulkIssueDTO response = WebClientUtils.post(webClient,endpoint,bulkIssueDTO,ResponseBulkIssueDTO.class).block();

        // 이슈 flag 변경
        String successMent = "";
        if(response != null){
            for(ResponseBulkIssueDTO.IssueDTO issueDTO : response.getIssues()){

                String issueKey = issueDTO.getKey();
                successMent += "["+issueKey+"] 해당 이슈가 WSS에 작성되어 지라에 생성되었습니다. \n";

                transferIssue.changeIssueStatus(issueKey);
            }
        }
        Date currentTime = new Date();
        SaveLog.SchedulerResult("ISSUE\\WSS",successMent,currentTime);
        return response;
    }

    @Override
    public void updateIssueByScheduler(int page, int size) throws Exception {

        Pageable pageable = PageRequest.of(page, size);
        Page<TB_JML_Entity> jmlEntityPage = TB_JML_JpaRepository.findAllByUpdateIssueFlagFalseOrderByMigratedDateDesc(pageable);

        for(TB_JML_Entity updateProject : jmlEntityPage){
            String projectCode = updateProject.getProjectCode();
            TransferIssueDTO transferIssueDTO = new TransferIssueDTO();
            transferIssueDTO.setProjectCode(projectCode);
            transferIssue.updateIssueData(transferIssueDTO);
        }
    }

}
