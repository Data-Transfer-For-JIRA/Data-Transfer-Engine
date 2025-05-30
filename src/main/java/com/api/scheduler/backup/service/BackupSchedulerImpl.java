package com.api.scheduler.backup.service;

import com.api.scheduler.backup.model.entity.BACKUP_BASEINFO_M_Entity;
import com.api.scheduler.backup.model.entity.BACKUP_BASEINFO_P_Entity;
import com.api.scheduler.backup.model.entity.BACKUP_ISSUE_COMMENT_Entity;
import com.api.scheduler.backup.model.entity.BACKUP_ISSUE_Entity;
import com.jira.issue.model.dao.PJ_PG_SUB_JpaRepository;
import com.api.scheduler.backup.model.dao.BACKUP_BASEINFO_M_JpaRepository;
import com.api.scheduler.backup.model.dao.BACKUP_BASEINFO_P_JpaRepository;
import com.api.scheduler.backup.model.dao.BACKUP_ISSUE_COMMENT_JpaRepository;
import com.api.scheduler.backup.model.dao.BACKUP_ISSUE_JpaRepository;
import com.jira.account.service.Account;
import com.jira.issue.model.FieldInfo;
import com.jira.issue.model.FieldInfoCategory;
import com.jira.issue.model.dto.FieldDTO;
import com.jira.issue.model.dto.comment.CommentDTO;
import com.jira.issue.model.dto.search.*;
import com.jira.issue.model.dto.weblink.SearchWebLinkDTO;
import com.jira.issue.model.entity.PJ_PG_SUB_Entity;
import com.jira.issue.service.JiraIssue;
import com.jira.project.model.dao.TB_JML_JpaRepository;
import com.jira.project.model.dto.CreateProjectDTO;
import com.jira.project.model.dto.ProjectDTO;
import com.jira.project.model.entity.TB_JML_Entity;
import com.jira.project.service.JiraProject;
import com.utils.SaveLog;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AllArgsConstructor
@Service("backupScheduler")
@EnableAsync
public class BackupSchedulerImpl implements BackupScheduler {

    @Autowired
    JiraProject jiraProject;

    @Autowired
    JiraIssue jiraIssue;

    @Autowired
    Account account;

    @Autowired
    private WebClientUtils webClientForImage;

    @Autowired
    private TB_JML_JpaRepository TB_JML_JpaRepository;

    @Autowired
    private BACKUP_BASEINFO_M_JpaRepository BACKUP_BASEINFO_M_JpaRepository;

    @Autowired
    private BACKUP_BASEINFO_P_JpaRepository BACKUP_BASEINFO_P_JpaRepository;

    @Autowired
    private BACKUP_ISSUE_JpaRepository BACKUP_ISSUE_JpaRepository;

    @Autowired
    private PJ_PG_SUB_JpaRepository PJ_PG_SUB_JpaRepository;

    @Autowired
    private BACKUP_ISSUE_COMMENT_JpaRepository BACKUP_ISSUE_COMMENT_JpaRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String[] prefixFormat = {
            "ED-P_WSS_",
            "ED-M_WSS_",
            "ED-P_",
            "ED-M_"
    };

    private static final String JIRA_PROJECT_PREFIX = "P_";
    private static final String JIRA_MAINTENANCE_PREFIX = "M_";
    private static final Pattern REMOVE_PREFIX = Pattern.compile("ED-P_|ED-M_|WSS_|EP-|EM-|P_|M_|\\?");

    @Async
    @Override
    public CompletableFuture<Void> 지라_프로젝트이름_수정() throws Exception {
        logger.info("[::BackupSchedulerImpl::] 지라 프로젝트 이름 벌크 수정 스케줄러");
        int page = 0;
        int size = 100;
        Pageable pageable = PageRequest.of(page, size);
        Page<TB_JML_Entity> 프로젝트_페이지;
        do {
            프로젝트_페이지 = TB_JML_JpaRepository.findAll(pageable);
            List<TB_JML_Entity> 모든_프로젝트 = 프로젝트_페이지.getContent();
            List<CompletableFuture<Void>> futures = 모든_프로젝트.stream()
                    .map(프로젝트 -> {
                        try {
                            return 지라프로젝트_JML테이블_업데이트(프로젝트);
                        } catch (Exception e) {
                            logger.error("프로젝트 이름 업데이트 중 오류 발생 프로젝트 키: {} \n 로그: {}",프로젝트.getKey(),e.getMessage());
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            // 모든 비동기 작업이 완료될 때까지 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            pageable = 프로젝트_페이지.nextPageable();
        } while (프로젝트_페이지.hasNext());

        return CompletableFuture.completedFuture(null);

    }

    @Transactional
    private CompletableFuture<Void> 지라프로젝트_JML테이블_업데이트(TB_JML_Entity 프로젝트) throws Exception {
        Date currentTime = new Date();
        String message = "[" + 프로젝트.getKey() + "] - " + currentTime + " - ";

        try {

            // DB에 저장된 프로젝트 정보
            String 저장된_프로젝트_이름 = 프로젝트.getJiraProjectName();
            String 프로젝트_이름 = null;

            // prefixFormat에 있는 접두사 제거
            for (String prefix : prefixFormat) {
                if (저장된_프로젝트_이름.startsWith(prefix)) {
                    프로젝트_이름 = 저장된_프로젝트_이름.substring(prefix.length());
                    break;
                }
            }

            if (프로젝트_이름 == null) {
                logger.info("프로젝트 이름에 접두사가 없습니다. --> {}", 저장된_프로젝트_이름);
            }

            boolean isUpdated = false;
            CreateProjectDTO 업데이트_정보 = new CreateProjectDTO();
            업데이트_정보.setKey(프로젝트.getKey());

            // 프로젝트 이름 업데이트
            if (프로젝트_이름 != null && !StringUtils.equals(저장된_프로젝트_이름, 프로젝트_이름)) {
                업데이트_정보.setName(프로젝트_이름);
                message += " 프로젝트 이름이 " + 저장된_프로젝트_이름 + "에서 "+프로젝트_이름 + "로 업데이트 되었습니다. \n";
                isUpdated = true;
            }

            if (isUpdated) {
                Map<String, String> result = jiraProject.updateProjectInfo(업데이트_정보);
                if (result != null && StringUtils.equals(result.get("projectResult"), "UPDATE_SUCCESS")) {
                    logger.info(message);
                } else {
                    logger.info("프로젝트 이름 업데이트에 실패했습니다. {} --실패--> {}", 저장된_프로젝트_이름, 프로젝트_이름);
                }
            }

        } catch (Exception e) {
            message += e.getMessage()+"프로젝트 이름 업데이트 중 오류 발생";
            logger.error(message);
            throw new Exception(e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    /*
    *  프로젝트 백업은 프로젝트 이름, 담당자 정보, 업데이트 시간 정보를 백업 및 업데이트 진행
    *  프로젝트 코드 추가
    * */
    @Override
    public CompletableFuture<Void> 지라프로젝트_백업() throws Exception {
        logger.info("[::BackupSchedulerImpl::] 지라 프로젝트 정보 벌크 백업 스케줄러");
        int page = 0;
        int size = 100;
        Pageable pageable = PageRequest.of(page, size);
        Page<TB_JML_Entity> 프로젝트_페이지;
        do {
            프로젝트_페이지 = TB_JML_JpaRepository.findAll(pageable);
            List<TB_JML_Entity> 모든_프로젝트 = 프로젝트_페이지.getContent();
            List<CompletableFuture<Void>> futures = 모든_프로젝트.stream()
                    .map(프로젝트 -> {
                        try {
                            return 지라프로젝트_JML테이블_업데이트(프로젝트.getKey(),프로젝트);
                        } catch (Exception e) {
                            logger.error("프로젝트 정보 업데이트 중 오류 발생 프로젝트 키: {} \n 로그: {}",프로젝트.getKey(),e.getMessage());
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            // 모든 비동기 작업이 완료될 때까지 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            pageable = 프로젝트_페이지.nextPageable();
        } while (프로젝트_페이지.hasNext());

        return CompletableFuture.completedFuture(null);

    }

    @Transactional
    private CompletableFuture<Void> 지라프로젝트_JML테이블_업데이트(String 지라_프로젝트_키, TB_JML_Entity 프로젝트) throws Exception {
        Date currentTime = new Date();
        String message = "[" + 지라_프로젝트_키 + "] - " + currentTime + " - ";

        /*if (!StringUtils.equals(지라_프로젝트_키, "TED779")) {
            return CompletableFuture.completedFuture(null);
        }
        */
        try {

            // DB에 저장된 프로젝트 정보
            String 저장된_프로젝트_이름 = 프로젝트.getJiraProjectName();
            String 저장된_프로젝트_코드 = 프로젝트.getProjectCode();
            String 저장된_담당자 = 프로젝트.getJiraProjectLeader();
            logger.info("기존 프로젝트 정보: {}, {}, {}", 저장된_프로젝트_이름, 저장된_프로젝트_코드, 저장된_담당자);

            // 지라에서 조회한 프로젝트 정보
            ProjectDTO 조회한_프로젝트_정보 = jiraProject.getJiraProjectInfoByJiraKey(지라_프로젝트_키);
            String 프로젝트_이름 = 조회한_프로젝트_정보.getName();

            // 기본 정보 이슈 티켓 조회 (프로젝트 코드 및 담당자 조회)
            String 기본정보_이슈키 = jiraIssue.getBaseIssueKeyByJiraKey(지라_프로젝트_키);
            if (기본정보_이슈키 == null) {
                logger.error(":: 지라프로젝트_JML테이블_업데이트 :: 기본정보_이슈키가 null입니다. 프로젝트 키: " + 지라_프로젝트_키);
                return CompletableFuture.completedFuture(null);
            }
            SearchIssueDTO<SearchProjectInfoDTO> 프로젝트_기본정보;
            SearchIssueDTO<SearchMaintenanceInfoDTO> 유지보수_기본정보;
            String 담당자_이름 = null;
            String 프로젝트_코드 = null;

            if (StringUtils.equals(프로젝트.getFlag(), "P")) {
                프로젝트_기본정보 = jiraIssue.getProjectIssue(기본정보_이슈키);

                if (프로젝트_기본정보 != null && 프로젝트_기본정보.getFields() != null) {
                    담당자_이름 = Optional.ofNullable(프로젝트_기본정보.getFields().getAssignee())
                            .map(FieldDTO.User::getDisplayName)
                            .orElse(""); // 담당자가 없을 경우 기본값 설정

                    프로젝트_코드 = Optional.ofNullable(프로젝트_기본정보.getFields().getProjectCode())
                            .orElse(""); // 프로젝트 코드가 없을 경우 기본값 설정
                }
            } else {
                유지보수_기본정보 = jiraIssue.getMaintenanceIssue(기본정보_이슈키);

                if (유지보수_기본정보 != null && 유지보수_기본정보.getFields() != null) {
                    담당자_이름 = Optional.ofNullable(유지보수_기본정보.getFields().getAssignee())
                            .map(FieldDTO.User::getDisplayName)
                            .orElse(""); // 담당자가 없을 경우 기본값 설정

                    프로젝트_코드 = Optional.ofNullable(유지보수_기본정보.getFields().getMaintenanceCode())
                            .orElse(""); // 유지보수 코드가 없을 경우 기본값 설정
                }
            }

            String 가공한_담당자_이름 = account.이름_추출(담당자_이름);
            if (StringUtils.isEmpty(가공한_담당자_이름)) {
                가공한_담당자_이름 = "epage div";
            }
            logger.info("지라 프로젝트 정보: {}, {}, {}", 프로젝트_이름, 프로젝트_코드, 가공한_담당자_이름);

            // 변경된 부분만 업데이트
            boolean isUpdated = false;

            // 프로젝트 이름 업데이트
            if (!StringUtils.equals(저장된_프로젝트_이름, 프로젝트_이름)) {
                프로젝트.setJiraProjectName(프로젝트_이름);
                message += " 프로젝트 이름이 " + 저장된_프로젝트_이름 + "에서 "+프로젝트_이름 + "로 업데이트 되었습니다. \n";
                isUpdated = true;
            }
            // 프로젝트 코드 업데이트
            if (!StringUtils.equals(저장된_프로젝트_코드, 프로젝트_코드)) {
                프로젝트.setProjectCode(프로젝트_코드);
                message += " 프로젝트 코드가 " + 저장된_프로젝트_코드 + "에서 " + 프로젝트_코드 + "로 업데이트 되었습니다. \n";
                isUpdated = true;
            }
            // 담당자 정보 업데이트
            if (!StringUtils.equals(저장된_담당자, 가공한_담당자_이름)) {
                프로젝트.setJiraProjectLeader(가공한_담당자_이름);
                message += " 프로젝트 담당자 정보가 " + 저장된_담당자 + "에서 " + 가공한_담당자_이름 + "로 업데이트 되었습니다. \n";
                isUpdated = true;
            }
            logger.info("isUpdated 상태: {}", isUpdated);

            if (isUpdated) { // 변경 사항 있을 때 업데이트
                프로젝트.setUpdateIssueFlag(true);
                TB_JML_JpaRepository.save(프로젝트);
            } else {
                logger.info("변경된 사항이 없어 업데이트를 수행하지 않았습니다.");
            }

            logger.info("업데이트 정보: {}", 프로젝트.toString());

        } catch (Exception e) {
            message += e.getMessage()+"프로젝트 정보 업데이트 중 오류 발생";
            logger.error(message);
            throw new Exception(e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Transactional
    public void 지라이슈_데일리_백업() throws Exception{

        List<String> 프로젝트기본정보_이슈키목록 = new ArrayList<>();
        List<String> 유지보수기본정보_이슈키목록 = new ArrayList<>();
        List<String> 일반_이슈키목록 = new ArrayList<>();
        List<String> 댓글_이슈키목록 = new ArrayList<>();

        // 오늘 생성 및 업데이트 된 이슈 조회 (댓글 생성 및 업데이트된 경우도 포함)
        List<SearchIssueDTO<FieldDTO>> 이슈목록 = jiraIssue.오늘_업데이트및_생성된이슈들().getIssues();
        //이슈목록.forEach(issue -> System.out.println(issue));

        String 프로젝트_기본정보 = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, "프로젝트 기본 정보").getId();
        String 유지보수_기본정보  = FieldInfo.ofLabel(FieldInfoCategory.ISSUE_TYPE, "유지보수 기본 정보").getId();

        // 생성 업데이트 된 키 목록 분류
        이슈목록.forEach(issue -> {
            댓글_이슈키목록.add(issue.getKey());
            if (issue.getFields().getIssuetype().getId().equals(프로젝트_기본정보) ) {
                프로젝트기본정보_이슈키목록.add(issue.getKey());
            }
            else if(issue.getFields().getIssuetype().getId().equals(유지보수_기본정보)){
                유지보수기본정보_이슈키목록.add(issue.getKey());
            }
            else {
                일반_이슈키목록.add(issue.getKey());
            }
        });

        // 프로젝트 기본정보 이슈 생성·업데이트
        if (프로젝트기본정보_이슈키목록 != null && !프로젝트기본정보_이슈키목록.isEmpty()) {
            프로젝트기본정보_이슈키목록.forEach(이슈키 -> {
                try {
                    프로젝트_기본정보이슈_저장(jiraIssue.getProjectIssue(이슈키));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        // 유지보수 기본정보 이슈 생성·업데이트
        if (유지보수기본정보_이슈키목록 != null && !유지보수기본정보_이슈키목록.isEmpty()) {
            유지보수기본정보_이슈키목록.forEach(이슈키 -> {
                try {
                    유지보수_기본정보이슈_저장(jiraIssue.getMaintenanceIssue(이슈키));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // 일반 이슈 백업
        if (일반_이슈키목록 != null && !일반_이슈키목록.isEmpty()) {
            일반_이슈키목록.forEach(이슈키 -> {
                try {
                    SearchRenderedIssue 이슈 = jiraIssue.이슈_조회(이슈키);
                    String 프로젝트키 = 이슈.getFields().getProject().getKey();
                    지라이슈_저장(이슈, 프로젝트키);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // 댓글 백업
        댓글_이슈키목록.forEach(이슈키 -> {
            try {
                오늘_지라이슈_댓글저장(이슈키);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /*
     *  기본정보 이슈 데이터를 백업
     * */
    @Async
    @Override
    @Transactional
    public CompletableFuture<Void> 지라기본정보_벌크_백업() throws Exception {
        logger.info("[::BackupSchedulerImpl::] 지라 기본 정보 벌크 백업 스케줄러");
        int page = 0;
        int size = 100;
        Pageable pageable = PageRequest.of(page, size);

        Page<TB_JML_Entity> 프로젝트_페이지;
        do {
            프로젝트_페이지 = TB_JML_JpaRepository.findAll(pageable);
            List<TB_JML_Entity> 모든_프로젝트 = 프로젝트_페이지.getContent();
            List<CompletableFuture<Void>> futures = 모든_프로젝트.stream()
                    .map(프로젝트 -> 지라기본정보_처리(프로젝트))
                    .collect(Collectors.toList());

            // 모든 비동기 작업이 완료될 때까지 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            pageable = 프로젝트_페이지.nextPageable();
        } while (프로젝트_페이지.hasNext());

        return CompletableFuture.completedFuture(null);
    }

    @Async
    private CompletableFuture<Void> 지라기본정보_처리(TB_JML_Entity 프로젝트) {
        try {
            String 지라_프로젝트_키 = 프로젝트.getKey();
            String 프로젝트_유형 = 프로젝트.getFlag();
            String 기본정보_이슈키 = jiraIssue.getBaseIssueKeyByJiraKey(지라_프로젝트_키);

            logger.info("[::BackupSchedulerImpl::] 저장 대상 프로젝트 키 정보: {}, 이슈 키:{} ",지라_프로젝트_키,기본정보_이슈키);

            if (기본정보_이슈키 != null) {
                if ("M".equals(프로젝트_유형)) {
                    SearchIssueDTO<SearchMaintenanceInfoDTO> 유지보수_기본_정보 = jiraIssue.getMaintenanceIssue(기본정보_이슈키);
                    유지보수_기본정보이슈_저장(유지보수_기본_정보);
                } else {
                    SearchIssueDTO<SearchProjectInfoDTO> 프로젝트_기본_정보 = jiraIssue.getProjectIssue(기본정보_이슈키);
                    프로젝트_기본정보이슈_저장(프로젝트_기본_정보);
                }
            } else {
                logger.warn("기본정보 이슈키가 null입니다. 프로젝트 키: {}", 지라_프로젝트_키);
            }
        } catch (Exception e) {
            logger.error("Error processing project key" + 프로젝트.getKey(), e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /*
     *  기본정보 이슈 저장
     *  단일 프로젝트 정보 저장
     * */
    @Override
    @Transactional
    public Boolean 기본정보이슈_저장(String 지라_키,String 프로젝트_유형) throws Exception{
        try {
            String 기본정보_이슈키=null;
            기본정보_이슈키 = jiraIssue.getBaseIssueKeyByJiraKey(지라_키);
            logger.info("[{}] 기본정보 이슈 저장 시작",기본정보_이슈키);
            if(기본정보_이슈키 == null){
                logger.error("{} 해당 프로젝트에는 기본 정보이슈가 없습니다.",지라_키);
            }

            if(프로젝트_유형.equals("M")){
                SearchIssueDTO<SearchMaintenanceInfoDTO> 조회결과 = jiraIssue.getMaintenanceIssue(기본정보_이슈키);
                if (조회결과 == null || 조회결과.getFields() == null) {
                    throw new Exception("유효한 조회 결과가 없습니다.");
                }
                유지보수_기본정보이슈_저장(조회결과);

                return true;
            }
            else{
                SearchIssueDTO<SearchProjectInfoDTO> 조회결과 = jiraIssue.getProjectIssue(기본정보_이슈키);
                if (조회결과 == null || 조회결과.getFields() == null) {
                    throw new Exception("유효한 조회 결과가 없습니다.");
                }
                프로젝트_기본정보이슈_저장(조회결과);
                return true;
            }
        }catch (Exception e){
            logger.error("기본정보 이슈 저장시 저장 오류 발생 : "+e.getMessage());
            return false;
        }
    }

    public static <T> T 값_널체크(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (NullPointerException e) {
            return null;
        }
    }
    private String 담당자_이름_편집하기(String 지라에서준_이름){
        if(지라에서준_이름 == null || 지라에서준_이름.isEmpty()){
            return null;
        }
        String 담당자_이름;
        if(지라에서준_이름.contains("(")){
            int startIndex = 지라에서준_이름.indexOf("(");
            담당자_이름 = 지라에서준_이름.substring(0, startIndex).trim(); // 대부분 모든 사람의 이름 뒤에 영어 이름이 붙어 나옴
        }else{
            담당자_이름 = 지라에서준_이름; // epage dev 케이스
        }

        return 담당자_이름;
    }
    private String 제품정보_변환(List<FieldDTO.Field> 제품정보) {
        if (제품정보 == null) {
            return null;
        }
        return 제품정보.stream()
                .map(FieldDTO.Field::getValue)
                .collect(Collectors.joining(" "));
    }
    private String 연관된_프로젝트_키_가져오기(List<SearchWebLinkDTO> 웹링크_조회_결과) {
        if (웹링크_조회_결과.size() > 0) {
            StringBuilder 프로젝트_키_빌더 = new StringBuilder();

            for (SearchWebLinkDTO 웹_링크 : 웹링크_조회_결과) {
                String url = 웹_링크.getObject().getUrl();
                String pattern = "projects/([A-Z0-9]+)/board";
                Pattern compiledPattern = Pattern.compile(pattern);
                Matcher matcher = compiledPattern.matcher(url);

                // 매칭 여부 확인
                if (matcher.find()) {
                    String 이슈_키 = matcher.group(1);
                    프로젝트_키_빌더.append(이슈_키).append(",");
                } else {
                    // 매칭되지 않은 경우 처리
                    logger.info("웹링크 매칭되지 않은 URL {}", url);
                }
            }

            // 마지막 쉼표와 공백 제거
            if (프로젝트_키_빌더.length() > 0) {
                프로젝트_키_빌더.setLength(프로젝트_키_빌더.length() - 1);
            }

            return 프로젝트_키_빌더.toString();
        } else {
            return null;
        }
    }
    private Integer 유지보수_기본정보이슈_저장( SearchIssueDTO<SearchMaintenanceInfoDTO>  조회결과) throws Exception {
        try {
            logger.info("유지보수 기본정보 이슈 저장을 시작합니다");
            String 연관된_프로젝트_키 = null;
            String 이슈_키 = 값_널체크(() ->  조회결과.getKey());
            String 프로젝트_키 = 값_널체크(() -> 조회결과.getFields().getProject().getKey());
            String 유지보수_명 = 값_널체크(() -> 조회결과.getFields().getMaintenanceName());
            String 담당자_정 = 담당자_이름_편집하기(값_널체크(() -> 조회결과.getFields().getAssignee().getDisplayName()));
            String 담당자_부 = 담당자_이름_편집하기(값_널체크(() -> 조회결과.getFields().getSubAssignee().getDisplayName()));
            String 영업_대표 = 담당자_이름_편집하기(값_널체크(() -> 조회결과.getFields().getSalesManager().getDisplayName()));
            String 계약사 = 값_널체크(() -> 조회결과.getFields().getContractor());
            String 고객사 = 값_널체크(() -> 조회결과.getFields().getClient());
            String 바코드_타입 = 값_널체크(() -> 조회결과.getFields().getBarcodeType().getValue());
            String 멀티_OS_지원여부 = 값_널체크(() -> 조회결과.getFields().getMultiOsSupport().get(0).getValue());

            List<SearchWebLinkDTO> 연관된_프로젝트_정보 = jiraIssue.getWebLinkByJiraIssueKey(이슈_키);

            if (연관된_프로젝트_정보.size()>0){
                연관된_프로젝트_키 =연관된_프로젝트_키_가져오기( jiraIssue.getWebLinkByJiraIssueKey(이슈_키) );
            }

            String 제품정보1 = 제품정보_변환(조회결과.getFields().getProductInfo1());
            String 제품정보2 = 제품정보_변환(조회결과.getFields().getProductInfo2());
            String 제품정보3 = 제품정보_변환(조회결과.getFields().getProductInfo3());
            String 제품정보4 = 제품정보_변환(조회결과.getFields().getProductInfo4());
            String 제품정보5 = 제품정보_변환(조회결과.getFields().getProductInfo5());

            String 계약_여부 = 조회결과.getFields().getContractStatus().getValue() ;

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Date 유지보수_시작일 = null;
            String 지라_유지보수_시작일 = 값_널체크(() -> 조회결과.getFields().getMaintenanceStartDate());
            if (지라_유지보수_시작일 != null) {
                유지보수_시작일 = dateFormat.parse(지라_유지보수_시작일);
            }

            Date  유지보수_종료일 = null;
            String 지라_유지보수_종료일 = 값_널체크(() -> 조회결과.getFields().getMaintenanceEndDate());
            if(지라_유지보수_종료일 != null){
                유지보수_종료일 = dateFormat.parse(지라_유지보수_종료일);
            }

            String 점검_주기 = 값_널체크(() -> 조회결과.getFields().getInspectionCycle().getValue());
            String 점검_방법 = 값_널체크(() -> 조회결과.getFields().getInspectionMethod().getValue());
            String 프린터_지원_범위 =  값_널체크(() -> 조회결과.getFields().getPrinterSupportRange().getValue());

            BACKUP_BASEINFO_M_Entity 저장할_데이터 =  BACKUP_BASEINFO_M_Entity.builder()
                    .지라_프로젝트_키(프로젝트_키)
                    .유지보수_명(유지보수_명)
                    .담당자_정(담당자_정)
                    .담당자_부(담당자_부)
                    .영업_대표(영업_대표)
                    .계약사(계약사)
                    .고객사(고객사)
                    .바코드_타입(바코드_타입)
                    .멀티_OS_지원여부(멀티_OS_지원여부)
                    .연관된_프로젝트_키(연관된_프로젝트_키)
                    .제품_정보1(제품정보1)
                    .제품_정보2(제품정보2)
                    .제품_정보3(제품정보3)
                    .제품_정보4(제품정보4)
                    .제품_정보5(제품정보5)
                    .계약_여부(계약_여부)
                    .유지보수_시작일(유지보수_시작일)
                    .유지보수_종료일(유지보수_종료일)
                    .점검_주기(점검_주기)
                    .점검_방법(점검_방법)
                    .프린터_지원_범위(프린터_지원_범위)
                    .build();

            BACKUP_BASEINFO_M_JpaRepository.save(저장할_데이터);
            return 1;
        }catch (Exception e){
            logger.error("유지보수 기본정보 백업에 실패하였습니다.");
            return 0;
        }
    }
    private Integer 프로젝트_기본정보이슈_저장(SearchIssueDTO<SearchProjectInfoDTO> 조회결과) throws Exception{
        try {
            String 연관된_프로젝트_키 = null;
            String 이슈_키 = 값_널체크(() ->  조회결과.getKey());
            String 프로젝트_키 = 값_널체크(() -> 조회결과.getFields().getProject().getKey());
            String 프로젝트_명 = 값_널체크(() -> 조회결과.getFields().getProjectName());
            String 담당자_정 = 담당자_이름_편집하기(값_널체크(() -> 조회결과.getFields().getAssignee().getDisplayName()));
            String 담당자_부 = 담당자_이름_편집하기(값_널체크(() -> 조회결과.getFields().getSubAssignee().getDisplayName()));
            String 영업_대표 = 담당자_이름_편집하기(값_널체크(() -> 조회결과.getFields().getSalesManager().getDisplayName()));
            String 계약사 = 값_널체크(() -> 조회결과.getFields().getContractor());
            String 고객사 = 값_널체크(() -> 조회결과.getFields().getClient());
            String 바코드_타입 = 값_널체크(() -> 조회결과.getFields().getBarcodeType().getValue());
            String 멀티_OS_지원여부 = 값_널체크(() -> 조회결과.getFields().getMultiOsSupport().get(0).getValue());

            List<SearchWebLinkDTO> 연관된_프로젝트_정보 = jiraIssue.getWebLinkByJiraIssueKey(이슈_키);
            if (연관된_프로젝트_정보.size()>0){
                연관된_프로젝트_키 =연관된_프로젝트_키_가져오기( jiraIssue.getWebLinkByJiraIssueKey(이슈_키) );
            }

            String 제품정보1 = 제품정보_변환(조회결과.getFields().getProductInfo1());
            String 제품정보2 = 제품정보_변환(조회결과.getFields().getProductInfo2());
            String 제품정보3 = 제품정보_변환(조회결과.getFields().getProductInfo3());
            String 제품정보4 = 제품정보_변환(조회결과.getFields().getProductInfo4());
            String 제품정보5 = 제품정보_변환(조회결과.getFields().getProductInfo5());

            Date 프로젝트_배정일 = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String 지라_프로젝트_배정일 = 값_널체크(() -> 조회결과.getFields().getProjectAssignmentDate());
            if (지라_프로젝트_배정일 != null) {
                프로젝트_배정일 = dateFormat.parse(지라_프로젝트_배정일);
            }

            String 프로젝트_진행_단계 = 값_널체크(() -> 조회결과.getFields().getProjectProgressStep().getValue());

            String 프린터_지원_범위 = 값_널체크(() -> 조회결과.getFields().getPrinterSupportRange().getValue());

            BACKUP_BASEINFO_P_Entity 저장할_데이터 =  BACKUP_BASEINFO_P_Entity.builder()
                    .지라_프로젝트_키(프로젝트_키)
                    .프로젝트_명(프로젝트_명)
                    .담당자_정(담당자_정)
                    .담당자_부(담당자_부)
                    .영업_대표(영업_대표)
                    .계약사(계약사)
                    .고객사(고객사)
                    .바코드_타입(바코드_타입)
                    .멀티_OS_지원여부(멀티_OS_지원여부)
                    .연관된_프로젝트_키(연관된_프로젝트_키)
                    .제품_정보1(제품정보1)
                    .제품_정보2(제품정보2)
                    .제품_정보3(제품정보3)
                    .제품_정보4(제품정보4)
                    .제품_정보5(제품정보5)
                    .프로젝트_배정일(프로젝트_배정일)
                    .프로젝트_진행_단계(프로젝트_진행_단계)
                    .프린터_지원_범위(프린터_지원_범위)
                    .build();

            BACKUP_BASEINFO_P_JpaRepository.save(저장할_데이터);
            return 1;
        }catch (Exception e){
            logger.error("프로젝트 기본정보 백업에 실패하였습니다.");
            return 0;
        }

    }


    /* 2. 주기적인 데이터 백업
     *  - wss 이슈를 제외한 나머지 이슈중 업데이트가 있는 이슈를 조회하여 백업
     *  - 삭제 된것이 있는지 확인 필요 이슈키도 저장 updated >= startOfDay() OR created >= startOfDay()
     * */
    @Override
    @Transactional
    public void 지라이슈_벌크_백업() throws Exception {
        logger.info("[::BackupSchedulerImpl::] 지라 이슈 벌크 백업 스케줄러");

        int page = 0;
        int size = 100;
        Pageable pageable = PageRequest.of(page, size);

        Page<TB_JML_Entity> 프로젝트_페이지;

        do {
            프로젝트_페이지 = TB_JML_JpaRepository.findAll(pageable);
            List<TB_JML_Entity> 모든_프로젝트 = 프로젝트_페이지.getContent();

            for (TB_JML_Entity 프로젝트 : 모든_프로젝트) {
                지라이슈_처리(프로젝트);
            }

            pageable = 프로젝트_페이지.nextPageable();
        } while (프로젝트_페이지.hasNext());

        logger.info("[::BackupSchedulerImpl::] 지라 이슈 벌크 백업 완료");
    }
    @Override
    @Transactional
    public void 지라이슈_저장(String 지라프로젝트_키) throws Exception{
        logger.info("[::지라이슈_저장::] 단일 프로젝트 지라 이슈 저장 스케줄러");
        TB_JML_Entity 프로젝트 = TB_JML_JpaRepository.findByKey(지라프로젝트_키);

        if(프로젝트 == null){
            logger.error("[::지라이슈_저장::] 지라 이슈 테이블 조회 오류 발생: {}",지라프로젝트_키);
            throw new Exception("[::지라이슈_저장::] 지라 이슈 테이블 조회 오류 발생: {} "+ 지라프로젝트_키);
        }

        try{
            지라이슈_처리(프로젝트);
        }catch (Exception e){
            logger.error("[::지라이슈_저장::] 최종 저장 장애 발생 --->"+e.getMessage());
        }
    }


    private void 지라이슈_처리(TB_JML_Entity 프로젝트) {
        String 프로젝트_키 = 프로젝트.getKey();
        List<SearchRenderedIssue> 전체이슈_목록 = new ArrayList<>();
        logger.info("[::지라이슈_저장::] 프로젝트에 생성된 이슈 정보 저장 : 프로젝트 키 = "+프로젝트_키);
        try {
            int 검색_시작_지점 = 0;
            int 검색_최대_개수 = 50;
            boolean isLast = false;

            while (!isLast) {
                프로젝트에_생성된_이슈데이터 이슈_조회결과 =
                        jiraIssue.프로젝트에_생성된_이슈조회(프로젝트_키, 검색_시작_지점, 검색_최대_개수);

                전체이슈_목록.addAll(이슈_조회결과.getIssues());

                if (검색_시작_지점 + 검색_최대_개수 >= 이슈_조회결과.getTotal()) {
                    isLast = true;
                } else {
                    검색_시작_지점 += 검색_최대_개수;
                }
            }

        } catch (Exception e) {
            logger.error("이슈 조회 중 오류 발생 - 프로젝트 키: {}, 메시지: {}", 프로젝트_키, e.getMessage());
            return;
        }

        for (SearchRenderedIssue 지라이슈 : 전체이슈_목록) {
            try {
                String 본문 = Optional.ofNullable(지라이슈.getRenderedFields())
                        .map(f -> f.getDescription())
                        .orElse("");

                if (!지라이슈.getFields().getSummary().contains("WSS HISTORY")
                        && (본문 == null || !containsAtLeastTwo(본문))) {
                    지라이슈_저장(지라이슈, 프로젝트_키);
                    지라이슈_댓글저장(지라이슈.getKey());
                }

            } catch (Exception e) {
                logger.error("이슈 저장 중 오류 - 이슈 키: {}, 메시지: {}", 지라이슈.getKey(), e.getMessage());
            }
        }
    }


    public boolean containsAtLeastTwo(String text) {

        String target = "====================================================================";

        int firstIndex = text.indexOf(target);
        if (firstIndex == -1) return false;

        int secondIndex = text.indexOf(target, firstIndex + target.length());
        return secondIndex != -1;
    }

    private BACKUP_ISSUE_Entity 지라이슈_저장(SearchRenderedIssue 지라이슈, String 프로젝트_키) throws Exception{

        // 지라_이슈_키가 null인지 확인
        String 지라_이슈_키 = 지라이슈.getKey();
        if (지라_이슈_키 == null || 지라_이슈_키.isEmpty()) {
            throw new Exception("::[::지라이슈_저장::]::지라 이슈 키가 null이거나 빈 값이므로 저장할 수 없습니다.");
        }

        String 담당자 = null;
        if (지라이슈.getFields() != null && 지라이슈.getFields().getAssignee() != null) {
            담당자 = 지라이슈.getFields().getAssignee().getDisplayName();
        }

        String 상세_내용 = null;
        if (지라이슈.getRenderedFields() != null) {
            상세_내용 = 지라이슈.getRenderedFields().getDescription();
            try {
                이미지_저장(상세_내용, 지라_이슈_키);
            } catch (Exception e) {
                logger.warn("[::지라이슈_저장::] 이미지 저장 실패 - 이슈 키: {}, 메시지: {}", 지라_이슈_키, e.getMessage());
            }
        }

        String 지라_이슈_제목 = null;
        if (지라이슈.getFields() != null) {
            지라_이슈_제목 = 지라이슈.getFields().getSummary();
        }

        Date 생성일 = null;
        if (지라이슈.getRenderedFields() != null && 지라이슈.getFields().getCreated() != null) {
            생성일 = 일자변환기(지라이슈.getFields().getCreated());
        }

        Date 업데이트일 = null;
        if (지라이슈.getRenderedFields() != null && 지라이슈.getFields().getUpdated() != null) {
            업데이트일 = 일자변환기(지라이슈.getFields().getUpdated());
        }
        BACKUP_ISSUE_Entity 이슈_저장 = BACKUP_ISSUE_Entity.builder()
                                    .jiraProjectKey(프로젝트_키)
                                    .지라_이슈_키(지라_이슈_키)
                                    .지라_이슈_제목(지라_이슈_제목)
                                    .상세내용(상세_내용)
                                    .담당자(담당자)
                                    .createDate(생성일)
                                    .업데이트일(업데이트일)
                                    .이슈_출처(true)
                                    .build();

        BACKUP_ISSUE_Entity 저장_결과 = BACKUP_ISSUE_JpaRepository.save(이슈_저장);

        return 저장_결과;
    }

    private List<String> 이미지_저장(String 상세_내용, String 이슈_키) {

        String baseUrl = "https://markany.atlassian.net";
        List<String> 저장된이미지_경로목록 = new ArrayList<>();

        if (상세_내용 != null && 상세_내용.contains("<img")) {

            Pattern 이미지테그_패턴 = Pattern.compile("<img\\s+[^>]*src=\"([^\"]+)\"");
            Matcher 이미지테그_속성값 = 이미지테그_패턴.matcher(상세_내용);

            // alt 속성은 항상 존재하지 않을 수 있으므로, 해당 부분은 img 태그에서 같이 뽑자
            Pattern 전체_이미지태그_패턴 = Pattern.compile("<img\\s+[^>]*>");
            Matcher 이미지태그들 = 전체_이미지태그_패턴.matcher(상세_내용);

            while (이미지테그_속성값.find() && 이미지태그들.find()) {
                String srcValue = 이미지테그_속성값.group(1);
                String 요청_경로 = baseUrl + srcValue;

                String 이미지태그 = 이미지태그들.group();
                String 파일_이름 = 이슈_키 + "-image";

                Matcher altMatcher = Pattern.compile("alt=\"(.*?)\"").matcher(이미지태그);
                if (altMatcher.find() && !altMatcher.group(1).isBlank()) {
                    파일_이름 = 이슈_키 + "-" + altMatcher.group(1);
                }

                webClientForImage.downloadImage(요청_경로, 파일_이름);
                저장된이미지_경로목록.add(요청_경로);
            }
        }

        return 저장된이미지_경로목록;
    }

    private Date 일자변환기(String 일자) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        try {
            Date date = formatter.parse(일자);

            return date;
        } catch (Exception e) {
            throw new Exception("일자 변환 중 오류 "+e.getMessage());
        }
    }

    private void WSS이슈데이터_이관(SearchRenderedIssue 지라이슈, String 지라_프로젝트_키,String wss_프로젝트_코드){

        String 지라_이슈_제목 = 지라이슈.getFields().getSummary();
        String 지라_이슈_키 = 지라이슈.getKey();

        List<PJ_PG_SUB_Entity> wss_이슈조회_결과 = PJ_PG_SUB_JpaRepository.findAllByProjectCodeOrderByCreationDateDesc(wss_프로젝트_코드);

        List<BACKUP_ISSUE_Entity> 이슈_저장_리스트 = IntStream.range(0, wss_이슈조회_결과.size())
                .mapToObj(i -> {
                    PJ_PG_SUB_Entity wss_조회이슈 = wss_이슈조회_결과.get(i);

                    Date 이슈_생성일 = wss_조회이슈.getCreationDate();
                    String 이슈_내용 = wss_조회이슈.getIssueContent();
                    String 이슈_작성자 = wss_조회이슈.getWriter();

                    String 지라_이슈_키_wss = 지라_이슈_키 + "-" + (i + 1);

                    return BACKUP_ISSUE_Entity.builder()
                            .jiraProjectKey(지라_프로젝트_키)
                            .지라_이슈_키(지라_이슈_키_wss)
                            .지라_이슈_제목(지라_이슈_제목)
                            .상세내용(이슈_내용)
                            .담당자(이슈_작성자)
                            .createDate(이슈_생성일)
                            .업데이트일(이슈_생성일)
                            .이슈_출처(false)
                            .build();
                })
                .collect(Collectors.toList());
        // 배치 저장
        BACKUP_ISSUE_JpaRepository.saveAll(이슈_저장_리스트);
    }

    private List<BACKUP_ISSUE_COMMENT_Entity> 지라이슈_댓글저장(String 지라이슈_키) throws Exception {
        try {

            // 댓글 조회
            CommentDTO 조회된_댓글 = jiraIssue.이슈에_생성된_댓글조회(지라이슈_키);

            // 조회된 댓글이 없으면 빈 리스트 반환
            if (조회된_댓글 == null || 조회된_댓글.getComments() == null) {
                return Collections.emptyList();
            }

            List<BACKUP_ISSUE_COMMENT_Entity> 댓글_저장_리스트 = 조회된_댓글.getComments().stream()
                    .map(comments -> BACKUP_ISSUE_COMMENT_Entity.builder()
                            .댓글_아이디(comments.getId())
                            .댓글_내용(comments.getRenderedBody())
                            .생성일(comments.getCreated())
                            .업데이트일(comments.getUpdated())
                            .작성자(comments.getAuthor().getDisplayName())
                            .지라이슈_키(지라이슈_키)
                            .build())
                    .collect(Collectors.toList());

            return BACKUP_ISSUE_COMMENT_JpaRepository.saveAll(댓글_저장_리스트);
        }catch (Exception e){
            logger.error("::[::지라이슈_댓글저장::]::댓글 저장 중 오류 발생. 저장 오류 발생한 이슈 키 : "+지라이슈_키);
            throw new RuntimeException("댓글 저장 중 오류 발생");
        }
    }

    private List<BACKUP_ISSUE_COMMENT_Entity> 오늘_지라이슈_댓글저장(String 지라이슈_키) throws Exception {
        try {

            // 댓글 조회
            CommentDTO 조회된_댓글 = jiraIssue.오늘_업데이트및_생성된댓글들(지라이슈_키);

            // 조회된 댓글이 없으면 빈 리스트 반환
            if (조회된_댓글 == null || 조회된_댓글.getComments() == null) {
                return Collections.emptyList();
            }

            List<BACKUP_ISSUE_COMMENT_Entity> 댓글_저장_리스트 = 조회된_댓글.getComments().stream()
                    .map(comments -> BACKUP_ISSUE_COMMENT_Entity.builder()
                            .댓글_아이디(comments.getId())
                            .댓글_내용(comments.getRenderedBody())
                            .생성일(comments.getCreated())
                            .업데이트일(comments.getUpdated())
                            .작성자(comments.getAuthor().getDisplayName())
                            .지라이슈_키(지라이슈_키)
                            .build())
                    .collect(Collectors.toList());

            return BACKUP_ISSUE_COMMENT_JpaRepository.saveAll(댓글_저장_리스트);
        }catch (Exception e){
            logger.error("::[::지라이슈_댓글저장::]::댓글 저장 중 오류 발생. 저장 오류 발생한 이슈 키 : "+지라이슈_키);
            throw new RuntimeException("댓글 저장 중 오류 발생");
        }
    }


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

    public boolean updateSalesManager(String 지라_프로젝트키, String 기존_영업_담당자, String 실제_영업_담당자) throws Exception {

        String 담당자_이름 = null;
        if (실제_영업_담당자 != null) {
            담당자_이름 = account.이름_추출(실제_영업_담당자);
            if (StringUtils.isBlank(담당자_이름)) {
                담당자_이름 = "epage div";
            }
        }
        logger.info(":: 영업 담당자 업데이트 :: {}, {}, {}", 지라_프로젝트키, 기존_영업_담당자, 담당자_이름);

        if (!StringUtils.equals(담당자_이름, 기존_영업_담당자)) { // 디비 데이터와 비교해서 다르면 저장 및 업데이트

            TB_JML_Entity 업데이트_대상_프로젝트 = TB_JML_JpaRepository.findByKey(지라_프로젝트키);

            업데이트_대상_프로젝트.setJiraProjectSalesManager(담당자_이름);

            TB_JML_JpaRepository.save(업데이트_대상_프로젝트);

            return true;
        }
        return false;
    }

    @Override
    public void updateJMLSalesManager() throws Exception{
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
                            Date currentTime = new Date();
                            String scheduler_result_success = null;

                            // 디비 조회
                            String 지라_프로젝트키 = entity.getKey();
                            String 기존_영업_담당자 = entity.getJiraProjectSalesManager();

                            /*if (!StringUtils.equals(지라_프로젝트키, "TED779")) {
                                return;
                            }*/

                            // 지라 조회
                            String 기본정보_이슈키 = jiraIssue.getBaseIssueKeyByJiraKey(entity.getKey());
                            String 실제_영업_담당자;
                            if (기본정보_이슈키 == null) {
                                logger.error(":: 영업 담당자 할당 스케줄러 :: 기본정보_이슈키가 null입니다. 프로젝트 키: " + entity.getKey());
                                return;
                            }

                            if (StringUtils.equals(entity.getFlag(), "M")) {
                                SearchIssueDTO<SearchMaintenanceInfoDTO> 조회결과 = jiraIssue.getMaintenanceIssue(기본정보_이슈키);
                                if (조회결과 == null || 조회결과.getFields() == null) {
                                    throw new Exception("유효한 조회 결과가 없습니다. 프로젝트 키: " + 지라_프로젝트키 + ", 이슈 키: " + 기본정보_이슈키);
                                }
                                실제_영업_담당자 = Optional.ofNullable(조회결과)
                                        .map(result -> result.getFields())
                                        .map(fields -> fields.getSalesManager())
                                        .map(salesManager -> salesManager.getDisplayName())
                                        .orElse(null);
                            } else {
                                SearchIssueDTO<SearchProjectInfoDTO> 조회결과 = jiraIssue.getProjectIssue(기본정보_이슈키);
                                if (조회결과 == null || 조회결과.getFields() == null) {
                                    throw new Exception("유효한 조회 결과가 없습니다. 프로젝트 키: " + 지라_프로젝트키 + ", 이슈 키: " + 기본정보_이슈키);
                                }
                                실제_영업_담당자 = Optional.ofNullable(조회결과)
                                        .map(result -> result.getFields())
                                        .map(fields -> fields.getSalesManager())
                                        .map(salesManager -> salesManager.getDisplayName())
                                        .orElse(null);
                            }

                            logger.info(":: 영업 담당자 백업 스케줄러 :: 프로젝트 키 : " + 지라_프로젝트키 + ", 기본 정보 이슈 키 : " + 기본정보_이슈키 + ", 기존 영업 담당자 : " + 기존_영업_담당자 + ", 실제 영업 담당자 : " + 실제_영업_담당자);
                            Boolean 담당자_업데이트 = updateSalesManager(지라_프로젝트키, 기존_영업_담당자, 실제_영업_담당자);

                            if (담당자_업데이트) {
                                logger.info(":: 영업 담당자 백업 스케줄러 :: [" + 지라_프로젝트키 + "] 해당 프로젝트의 영업 담당자는 " + 실제_영업_담당자 + "로 재할당되었습니다.");
                            }
                        } catch (Exception e) {
                            logger.error(":: 영업 담당자 할당 스케줄러 :: 오류 발생 "+ e.getMessage());
                            throw new RuntimeException(e);
                        }
                    });

                    page++; // 다음 페이지로
                } catch (Exception e) {
                    logger.error(":: 영업 담당자 할당 스케줄러 :: 오류 발생 " + e.getMessage());
                    throw new Exception(e);
                }
            }
        } catch (Exception e) {
            logger.error(":: 영업 담당자 할당 스케줄러 :: 오류 발생 "+ e.getMessage());
            throw new Exception(e);
        }
    }

    @Override
    public void updateProjectNamePrefix() throws Exception {

        int page = 0;
        final int size = 100; // 한 페이지당 항목 수, 조정 가능

        while (true) {
                Pageable pageable = PageRequest.of(page, size);
                Page<TB_JML_Entity> entityPage = TB_JML_JpaRepository.findAll(pageable);

                if (!entityPage.hasContent()) {
                    break; // 더 이상 처리할 데이터가 없으면 반복 종료
                }

                entityPage.forEach(entity -> {
                    try {
                        prefixUpdate(entity);
                    } catch (Exception e) {
                        logger.error(":: 프로젝트 이름 prefix 세팅 스케줄러 :: 오류 발생 " + e.getMessage());
                    }
                });

                page++;
        }
    }

    public void prefixUpdate(TB_JML_Entity 프로젝트) throws Exception {

        Date currentTime = new Date();
        String message = "[" + 프로젝트.getKey() + "] - " + currentTime + " - ";

        String newPrefix = Optional.ofNullable(프로젝트.getFlag())
                .filter(flag -> "P".equals(flag))
                .map(flag -> JIRA_PROJECT_PREFIX)
                .orElse(JIRA_MAINTENANCE_PREFIX);

        String 기존_프로젝트명 = Optional.ofNullable(프로젝트.getJiraProjectName()).orElse("");
        String 새로운_프로젝트명 = REMOVE_PREFIX.matcher(기존_프로젝트명).replaceAll("").trim();
        새로운_프로젝트명 = newPrefix + 새로운_프로젝트명;

        // 프로젝트 이름 업데이트
        if (!StringUtils.equals(기존_프로젝트명, 새로운_프로젝트명)) {
            CreateProjectDTO 업데이트_정보 = new CreateProjectDTO();
            업데이트_정보.setKey(프로젝트.getKey());
            업데이트_정보.setName(새로운_프로젝트명);

            message += " 프로젝트 이름이 " + 기존_프로젝트명 + "에서 "+새로운_프로젝트명 + "로 업데이트 되었습니다. \n";

            Map<String, String> result = jiraProject.updateProjectInfo(업데이트_정보);
            if (result != null && StringUtils.equals(result.get("projectResult"), "UPDATE_SUCCESS")) {
                logger.info(message);
            } else {
                logger.info("프로젝트 이름 업데이트에 실패했습니다. {} --실패--> {}", 기존_프로젝트명, 새로운_프로젝트명);
            }
        }
    }

    @Override
    public CompletableFuture<Void> syncProject() {
        logger.info("[::BackupSchedulerImpl::] 지라 프로젝트 수정 스케줄러 시작");
        int page = 0;
        int size = 100;

        while (true) {
            Page<TB_JML_Entity> 페이지 = TB_JML_JpaRepository.findAll(PageRequest.of(page, size));
            List<TB_JML_Entity> 프로젝트들 = 페이지.getContent();

            List<CompletableFuture<Void>> futures = 프로젝트들.stream()
                    .map(프로젝트 -> 프로젝트_정보_업데이트_및_삭제(프로젝트)
                            .exceptionally(e -> {
                                logger.error("프로젝트 처리 중 예외 발생 - 키: {}", 프로젝트.getKey(), e);
                                return null;
                            }))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            if (!페이지.hasNext()) break;
            page++;
        }

        logger.info("[::BackupSchedulerImpl::] 지라 프로젝트 이름 벌크 수정 스케줄러 완료");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void syncSingleProject(String jiraKey) throws Exception{

        logger.info("[::BackupSchedulerImpl::] 단일 지라 프로젝트 이름 동기화 시작: {}", jiraKey);
        TB_JML_Entity 프로젝트 = TB_JML_JpaRepository.findByKey(jiraKey);

        if (프로젝트 == null) {
            logger.error("프로젝트 조회 실패: {}", jiraKey);
            throw new Exception("프로젝트 조회 실패: " + jiraKey);
        }

        try {
            프로젝트_정보_업데이트_및_삭제(프로젝트).join();
            logger.info("[::BackupSchedulerImpl::] 단일 지라 프로젝트 이름 동기화 완료: {}", jiraKey);
        } catch (Exception e) {
            logger.error("단일 프로젝트 동기화 중 예외 발생 - 키: {}", jiraKey, e);
            throw e;
        }

    }

    @Async
    private CompletableFuture<Void> 프로젝트_정보_업데이트_및_삭제(TB_JML_Entity 프로젝트) {
        return CompletableFuture.runAsync(() -> {
            String 프로젝트_키 = 프로젝트.getKey();
            try {
                ProjectDTO project = jiraProject.getJiraProjectInfoByJiraKey(프로젝트_키);
                if (project == null) {
                    logger.warn("프로젝트 조회 실패: {}", 프로젝트_키);
                    return;
                }

                String 저장할_이름 = 담당자_이름_편집하기(project.getLead().getDisplayName());
                프로젝트.setJiraProjectLeader(저장할_이름);
                프로젝트.setJiraProjectName(project.getName());

                TB_JML_JpaRepository.save(프로젝트);
            } catch (Exception e) {
                logger.warn("예외 발생 - 삭제 처리됨: {}", 프로젝트_키, e);
                TB_JML_JpaRepository.delete(프로젝트);
            }
        });
    }

}
