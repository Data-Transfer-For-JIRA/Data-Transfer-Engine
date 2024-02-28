package com.api.scheduler.migrate.issue.service;

import com.api.scheduler.migrate.issue.model.bulk.ResponseBulkIssueDTO;
import com.jira.issue.model.dao.PJ_PG_SUB_JpaRepository;
import com.jira.issue.model.dto.FieldDTO;
import com.jira.issue.model.dto.TransferIssueDTO;
import com.api.scheduler.migrate.issue.model.bulk.CreateBulkIssueDTO;
import com.api.scheduler.migrate.issue.model.bulk.CreateBulkIssueFieldsDTO;
import com.jira.issue.model.dto.weblink.RequestWeblinkDTO;
import com.jira.issue.model.dto.weblink.SearchWebLinkDTO;
import com.jira.issue.model.entity.PJ_PG_SUB_Entity;
import com.jira.issue.service.JiraIssue;
import com.jira.project.model.dao.TB_JLL_JpaRepository;
import com.jira.project.model.dao.TB_JML_JpaRepository;
import com.jira.project.model.dao.TB_PJT_BASE_JpaRepository;
import com.jira.project.model.entity.TB_JLL_Entity;
import com.jira.project.model.entity.TB_JML_Entity;
import com.jira.project.service.JiraProject;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@Service("jiraIssueByScheduler")
public class JiraIssueBySchedulerImpl implements JiraIssueByScheduler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WebClientUtils webClientUtils;

    @Autowired
    private TB_JML_JpaRepository TB_JML_JpaRepository;

    @Autowired
    private TB_PJT_BASE_JpaRepository TB_PJT_BASE_JpaRepository;

    @Autowired
    private TB_JLL_JpaRepository TB_JLL_JpaRepository;

    @Autowired
    private PJ_PG_SUB_JpaRepository PJ_PG_SUB_JpaRepository;

    @Autowired
    JiraProject jiraProject;

    @Autowired
    JiraIssue jiraIssue;

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
                jiraIssue.transferIssueData(transferIssueDTO); // 이슈 생성 작업

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
        List<PJ_PG_SUB_Entity> issueList = PJ_PG_SUB_JpaRepository.findByIssueMigrateFlagIsFalse();
        createBulkIssue(issueList);
    }
    public ResponseBulkIssueDTO createBulkIssue(List<PJ_PG_SUB_Entity> issueList) throws Exception {
        logger.info("[::TransferIssueBySchedulerImpl::] createBulkIssue");

        CreateBulkIssueDTO bulkIssueDTO = new CreateBulkIssueDTO();

        List<CreateBulkIssueFieldsDTO> issueUpdates = new ArrayList<>();

        for(PJ_PG_SUB_Entity issueData : issueList){

            String wssAssignee = jiraIssue.getOneAssigneeId(issueData.getWriter());

            String wssProjectId = issueData.getProjectId();
            String wssProjectCode = issueData.getProjectCode();

            if (checkMigrateIssueFlag(wssProjectId, wssProjectCode)) {
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
                jiraIssue.setMigrateIssueFlag(wssProjectId, wssProjectCode);
            }
        }

        bulkIssueDTO.setIssueUpdates(issueUpdates);

        String endpoint ="/rest/api/3/issue/bulk";

        ResponseBulkIssueDTO response = webClientUtils.post(endpoint, bulkIssueDTO, ResponseBulkIssueDTO.class).block();

        // 이슈 flag 변경
        String successMent = "";
        if(response != null){
            for(ResponseBulkIssueDTO.IssueDTO issueDTO : response.getIssues()){

                String issueKey = issueDTO.getKey();
                successMent += "["+issueKey+"] 해당 이슈가 WSS에 작성되어 지라에 생성되었습니다. \n";

                jiraIssue.changeIssueStatus(issueKey);
            }
        }
        Date currentTime = new Date();
        SaveLog.SchedulerResult("ISSUE\\WSS",successMent,currentTime);
        return response;
    }

    @Override
    public void updateIssueByScheduler(int page, int size) throws Exception {

        Pageable pageable = PageRequest.of(page, size);
        Page<TB_JML_Entity> jmlEntityPage = TB_JML_JpaRepository.findAll(jiraIssue.hasDateTimeBeforeIsNull("updateDate"), pageable);

        for (TB_JML_Entity updateProject : jmlEntityPage){
            String projectCode = updateProject.getProjectCode();
            TransferIssueDTO transferIssueDTO = new TransferIssueDTO();
            transferIssueDTO.setProjectCode(projectCode);
            jiraIssue.updateIssueData(transferIssueDTO);
        }
    }

    @Override
    @Transactional
    public void updateWebLink(int size) throws Exception{
        // 디비에서 연관된 프로젝트가 있는 프로젝트 목록 조회
        logger.info("[::TransferIssueBySchedulerImpl::] 연결된 프로젝트 weblink 스케줄러");
        // 디비 조회 로직
        Pageable pageable = PageRequest.of(0, size);
        Page<TB_JLL_Entity> relationList = TB_JLL_JpaRepository.findAllByLinkCheckFlagIsFalse(pageable); // 연관 목록 조회

        // 웹링크 조회 로직
        for(TB_JLL_Entity project : relationList){
            String parentKey = project.getParentKey(); // 웹링크 걸 대상 부모 지라 키

            String childKey = project.getChildKey(); // 웹링크 걸 대상 자식 지라 키

            if(!project.getLinkCheckFlag()){ // 이관이 안되어있으면

                int type = checkWeblink(parentKey,childKey); // parentKey와 childKey를 상호 연결되어있는지 확인

                switch (type) {
                    case 1: // 상호간 연결된 경우
                        break;
                    case 2: // 부모에겐 연결 안됐는데 자식한테는 연결된 경우
                        createWeblink(parentKey, childKey);
                        break;
                    case 3: // 부모에겐 연결 됐는데 자식한테는 연결 안된 경우
                        createWeblink(childKey, parentKey);
                        break;
                    case 4: // 둘 다 연결 안된 경우
                        createWebLinkBothSidesForScheduler(parentKey, childKey);
                        break;
                    default:
                        logger.error("[::TransferIssueBySchedulerImpl::] 연결된 프로젝트 확인 중 오류 발생하였습니다.");
                }
            }
            setLinkCheckFlagTrue(project); // 웹링크 플레그 변경
        }
    }

    /*
    *  지라키로 연결되어있는 프로젝트 키 조회 하는 메서드
    * */
    public List<String> getWeblinkListByJiraKey(String jiraKey) throws Exception {
        logger.info("[::TransferIssueBySchedulerImpl::] 연결된 웹링크 확인 ---->"+jiraKey);
        List<SearchWebLinkDTO> webLinkList = jiraIssue.getWebLinkByJiraKey(jiraKey); // 해당 프로젝트에서 연결되어있는 웹링크 조회 (지라 서버)
        List<String>  webLinkProjectList = trimWebLinkSearchListToProjectList(webLinkList); // 웹링크에서 프로젝트 코드 추출 (지라에 등록된 웹링크 조회)
        return webLinkProjectList;
    }

    /*
     *  프로젝트 코드만 빼오는 로직
     * */
    public List<String> trimWebLinkSearchListToProjectList(List<SearchWebLinkDTO> webLinkList) throws Exception{

        List<String> projectList = new ArrayList<>();

        for(SearchWebLinkDTO webLink : webLinkList){
            String url = webLink.getObject().getUrl();
            Pattern pattern = Pattern.compile("projects/(.*?)/board");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                projectList.add(matcher.group(1));
            }
        }
        logger.info("[::TransferIssueBySchedulerImpl::] 웹링크 반환할 프로젝트 목록 ---->"+projectList);
        return projectList;
    }
    /*
    *  웹링크 걸려있는지 확인하는 로직
    * */
    public int checkWeblink(String parentKey , String childKey) throws Exception{
        logger.info("[::TransferIssueBySchedulerImpl::] 웹링크 연결 검사 ---->"+parentKey + "  " +childKey);
        List<String> parentWeblinkList = getWeblinkListByJiraKey(parentKey); // 부모키에 걸려있는 웹링크 조회

        List<String> childWeblinkList = getWeblinkListByJiraKey(childKey); // 자식키에 걸려있는 웹링크 조회

        if(parentWeblinkList.contains(childKey) && childWeblinkList.contains(parentKey)) { // 상호간 연결된 경우
            logger.info("[::TransferIssueBySchedulerImpl::] 웹링크 양 방향 연결");
            return 1;
        } else if (!parentWeblinkList.contains(childKey) && childWeblinkList.contains(parentKey)) { // 부모에겐 연결 안됐는데 자식한테는 연결된 경우
            logger.info("[::TransferIssueBySchedulerImpl::] 웹링크 자식 프로젝트만 연결");
            return 2;
        }else if (parentWeblinkList.contains(childKey) && !childWeblinkList.contains(parentKey)){ // 부모에겐 연결 됐는데 자식한테는 연결 안된 경우
            logger.info("[::TransferIssueBySchedulerImpl::] 웹링크 부모 프로젝트만 연결");
            return 3;
        }else if (!parentWeblinkList.contains(childKey) && !childWeblinkList.contains(parentKey)){ // 둘다 연결 안된경우
            logger.info("[::TransferIssueBySchedulerImpl::] 웹링크 양 방향 연결 안됨");
            return 4;
        }
        return 0;
    }

    public Boolean createWeblink(String mainKey , String subKey) throws Exception{
        logger.info("[::TransferIssueBySchedulerImpl::] 웹링크 생성 시작");
        TB_JML_Entity subInfo =TB_JML_JpaRepository.findByKey(subKey);
        String issueKey = jiraIssue.getBaseIssueKeyByJiraKey(mainKey);
        String key = subInfo.getKey();
        String title = subInfo.getJiraProjectName();

        RequestWeblinkDTO requestWeblinkDTO = new RequestWeblinkDTO();
        requestWeblinkDTO.setJiraKey(key);
        requestWeblinkDTO.setTitle(title);
        requestWeblinkDTO.setIssueIdOrKey(issueKey);

        String result = jiraIssue.createWebLink(requestWeblinkDTO);

        if(result != null ){
            return true;
        }else{
            return false;
        }
    }


    /*
    *  이관 완료 flag 처리
    * */
    public void setLinkCheckFlagTrue(TB_JLL_Entity project) {
        Date currentTime = new Date();
        // id에 해당하는 TB_JLL_Entity를 가져옴
        Optional<TB_JLL_Entity> entity = TB_JLL_JpaRepository.findById(project.getId());

        if(entity.isPresent()){
            TB_JLL_Entity actualEntity = entity.get();
            // linkCheckFlag 값을 true로 설정
            actualEntity.setLinkCheckFlag(true);
            // 변경한 값을 저장
            TB_JLL_Entity savedEntity = TB_JLL_JpaRepository.save(actualEntity);
            if (savedEntity == null) {
                // 저장 실패
                String scheduler_result_fail = project.getParentKey() +"해당 키와 "+project.getChildKey()+" 해당 키의 웹링크 생성에 실패 하였습니다.";
                SaveLog.SchedulerResult("WEBLINK\\FAIL",scheduler_result_fail,currentTime);
            } else {
                // 저장 성공
                String scheduler_result_fail = project.getParentKey() +"해당 키와 "+project.getChildKey()+" 해당 키의 웹링크가 생성되었습니다.";
                SaveLog.SchedulerResult("WEBLINK\\SUCCESS",scheduler_result_fail,currentTime);
            }
        }

    }

    /*
     *  두개의 지라키로 양방향으로 웹링크 거는 로직 + 디비 업데이트
     * */
    public Boolean createWebLinkBothSidesForScheduler(String mainJiraKey, String subJiraKey) throws Exception {
        logger.info("[::TransferIssueImpl::] 웹링크 양방향 생성 -> " + mainJiraKey + "  " + subJiraKey);
        // mainJiraKey에 subJiraKey 걸기
        TB_JML_Entity mainInfo = TB_JML_JpaRepository.findByKey(mainJiraKey);
        TB_JML_Entity subInfo = TB_JML_JpaRepository.findByKey(subJiraKey);

        if (mainInfo != null && subInfo != null) {
            String mainIssueKeyOrId = jiraIssue.getBaseIssueKeyByJiraKey(mainJiraKey);
            String subTitle = subInfo.getJiraProjectName();

            RequestWeblinkDTO main = new RequestWeblinkDTO();
            main.setIssueIdOrKey(mainIssueKeyOrId);
            main.setJiraKey(subJiraKey);
            main.setTitle(subTitle);
            String mainResult = jiraIssue.createWebLink(main);

            // subJiraKey에 mainJiraKey 걸기
            String subIssueKeyOrId = jiraIssue.getBaseIssueKeyByJiraKey(subJiraKey);
            String mainTitle = mainInfo.getJiraProjectName();

            RequestWeblinkDTO sub = new RequestWeblinkDTO();
            sub.setIssueIdOrKey(subIssueKeyOrId);
            sub.setJiraKey(mainJiraKey);
            sub.setTitle(mainTitle);
            String subResult = jiraIssue.createWebLink(sub);

            if (mainResult != null && subResult != null) {
                TB_JLL_Entity entity = TB_JLL_JpaRepository.findByParentKeyAndChildKey(mainJiraKey, subJiraKey);
                entity.setLinkCheckFlag(true);
                TB_JLL_Entity savedEntity = TB_JLL_JpaRepository.save(entity);
                if (savedEntity != null) {
                    return true;
                }
            }
        }else{
            return false;
        }

        return false;
    }

    /*
     *  지라 이슈 이관 여부 체크
     * */
    @Override
    public boolean checkMigrateIssueFlag(String projectId, String projectCode) {

        logger.info("[::TransferIssueBySchedulerImpl::] checkMigrateIssueFlag");

        Optional<PJ_PG_SUB_Entity> entity = Optional.ofNullable(PJ_PG_SUB_JpaRepository.findByProjectIdAndProjectCode(projectId, projectCode));
        if (entity.isPresent()) {
            PJ_PG_SUB_Entity subEntity = entity.get();
            if (!subEntity.getIssueMigrateFlag()) {
                return true;
            }
        }
        return false;
    }

}
