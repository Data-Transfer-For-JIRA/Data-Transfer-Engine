package com.api.scheduler.wss.service;


import com.api.scheduler.backup.model.dao.BACKUP_BASEINFO_M_JpaRepository;
import com.api.scheduler.backup.model.dao.BACKUP_BASEINFO_P_JpaRepository;
import com.api.scheduler.backup.model.dao.BACKUP_ISSUE_JpaRepository;
import com.api.scheduler.backup.model.entity.BACKUP_BASEINFO_M_Entity;
import com.api.scheduler.backup.model.entity.BACKUP_BASEINFO_P_Entity;
import com.api.scheduler.backup.model.entity.BACKUP_ISSUE_Entity;
import com.jira.issue.model.dao.PJ_PG_SUB_JpaRepository;
import com.jira.issue.model.entity.PJ_PG_SUB_Entity;
import com.jira.project.model.dao.TB_JML_JpaRepository;
import com.jira.project.model.dao.TB_PJT_BASE_JpaRepository;
import com.jira.project.model.entity.TB_JML_Entity;
import com.jira.project.model.entity.TB_PJT_BASE_Entity;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@AllArgsConstructor
@Service("WssScheduler")
public class WssSchedulerImpl implements WssScheduler{


    private TB_JML_JpaRepository tb_jml_jpaRepository;

    private TB_PJT_BASE_JpaRepository tb_pjt_base_jpaRepository;

    private BACKUP_BASEINFO_M_JpaRepository backup_baseinfo_m_jpaRepository;

    private BACKUP_BASEINFO_P_JpaRepository backup_baseinfo_p_jpaRepository;

    private BACKUP_ISSUE_JpaRepository backup_issue_jpaRepository;

    private PJ_PG_SUB_JpaRepository pj_pg_sub_jpaRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    @Transactional
    public void syncAllProjectData() throws Exception {
        logger.info("2024년 이후 프로젝트 동기화 시작");
        List<TB_JML_Entity> 미생성_프로젝트 = tb_jml_jpaRepository.findProjectDateAfter2024();
        미생성_프로젝트.parallelStream().forEach(프로젝트 -> {
            try {
                saveWssProjectInfo(프로젝트);
            } catch (Exception e) {
                throw new RuntimeException("프로젝트 저장 중 오류 발생: " + 프로젝트.getKey(), e);
            }
        });
    }

    @Override
    @Transactional
    public TB_PJT_BASE_Entity syncSingleProject(String jiraProjectKey) throws Exception {
        logger.info("단일 프로젝트 프로젝트 생성 프로젝트 키: " + jiraProjectKey);
        TB_JML_Entity 프로젝트 = tb_jml_jpaRepository.findById(jiraProjectKey).orElse(null);
        return saveWssProjectInfo(프로젝트);
    }


    /*
    *  프로젝트에 생성된 이슈 백업 데이터를 WSS에 저장 합니다.
    * */
    @Override
    @Transactional
    public List<PJ_PG_SUB_Entity> syncSingleIssue(String jiraProjectKey) throws Exception{
        logger.info("단일 이슈 동기화 시작");

        // 1. 프로젝트 키 조회해야함
        List<BACKUP_ISSUE_Entity> 이슈목록 = backup_issue_jpaRepository.findByJiraProjectKey(jiraProjectKey);

        TB_JML_Entity 프로젝트 = tb_jml_jpaRepository.findById(jiraProjectKey).orElse(null);

        List<PJ_PG_SUB_Entity> 저장한_이슈목록 = new ArrayList<>();

        이슈목록.stream().forEach(이슈 ->{
            try {
                PJ_PG_SUB_Entity saveResult = saveIssue(이슈, 프로젝트);
                저장한_이슈목록.add(saveResult);
            } catch (Exception e) {
                throw new RuntimeException("이슈 저장 중 오류 발생: " + 이슈.get지라_이슈_키(), e);
            }
        });

        return 저장한_이슈목록;
    }


    @Override
    @Transactional
    public List<PJ_PG_SUB_Entity> syncAllIssue() throws Exception {
        logger.info("모든 이슈 동기화 시작");
        List<BACKUP_ISSUE_Entity> 이슈목록 = backup_issue_jpaRepository.findAll();
        List<PJ_PG_SUB_Entity> 저장한_이슈목록 = new ArrayList<>();

        이슈목록.parallelStream().forEach(이슈 -> {
            try {
                TB_JML_Entity 프로젝트 = tb_jml_jpaRepository.findById(이슈.getJiraProjectKey()).orElse(null);
                PJ_PG_SUB_Entity saveResult = saveIssue(이슈, 프로젝트);
                저장한_이슈목록.add(saveResult);
            } catch (Exception e) {
                throw new RuntimeException("이슈 저장 중 오류 발생: " + 이슈.get지라_이슈_키(), e);
            }
        });

        return 저장한_이슈목록;
    }


    @Override
    @Transactional
    public void syncProjectByScheduler() throws Exception {
        logger.info("스케줄러를 통한 프로젝트 정보 WSS로 동기화");
        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay(); // 오늘 00:00:00
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX); // 오늘 23:59:59.999999999

        List<TB_JML_Entity> 프로젝트_목록 = tb_jml_jpaRepository.findByMigratedDateBetween(startOfDay,endOfDay);

        프로젝트_목록.parallelStream().forEach(프로젝트 -> {
            try {
                saveWssProjectInfo(프로젝트);
            } catch (Exception e) {
                throw new RuntimeException("프로젝트 저장 중 오류 발생: " + 프로젝트.getKey(), e);
            }
        });

    }

    @Override
    @Transactional
    public void syncIssueByScheduler() throws Exception {
        logger.info("스케줄러를 통한 이슈 정보 WSS로 동기화");
        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay(); // 오늘 00:00:00
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX); // 오늘 23:59:59.999999999

        List<BACKUP_ISSUE_Entity> 이슈목록 = backup_issue_jpaRepository.findByCreateDateBetween(startOfDay,endOfDay);
        이슈목록.parallelStream().forEach(이슈 -> {
            try {
                TB_JML_Entity 프로젝트 = tb_jml_jpaRepository.findById(이슈.getJiraProjectKey()).orElse(null);
                PJ_PG_SUB_Entity saveResult = saveIssue(이슈, 프로젝트);
            } catch (Exception e) {
                throw new RuntimeException("이슈 저장 중 오류 발생: " + 이슈.get지라_이슈_키(), e);
            }
        });

    }


    private PJ_PG_SUB_Entity saveIssue(BACKUP_ISSUE_Entity 이슈, TB_JML_Entity 프로젝트) throws Exception {
        if (이슈 == null) return null;

        try {
            // 1. WSS에서 넘어온 프로젝트 이름 기반 조회
            String wssProjectName = 프로젝트.getWssProjectName();
            String wssProjectCode = 프로젝트.getProjectCode();
            String projectKey = 프로젝트.getKey();

            // 2. 프로젝트 코드로 먼저 조회 시도
            Optional<TB_PJT_BASE_Entity> table = Optional.empty();
            if (wssProjectCode != null && !wssProjectCode.isBlank()) {
                table = tb_pjt_base_jpaRepository.findById(wssProjectCode);
            }

            // 3. 없으면 프로젝트 이름으로 재조회
            if (table.isEmpty() && wssProjectName != null) {
                table = Optional.ofNullable(tb_pjt_base_jpaRepository.findByTrimmedProjectName(wssProjectName));
            }

            // 4. 프로젝트 키(예외 케이스)로 마지막 조회 시도
            if (table.isEmpty() && projectKey != null) {
                table = tb_pjt_base_jpaRepository.findById(projectKey);
            }

            // 5. 여전히 못 찾으면 예외 처리
            if (table.isEmpty()) {
                throw new RuntimeException("[중요!!] 해당 프로젝트 정보를 찾을 수 없습니다 프로젝트키 ---> " + 프로젝트.getKey());
            }

            // 6. 조회 성공 → 코드 확보
            wssProjectCode = table.get().getProjectCode();

            // 7. 프로젝트 ID 생성
            int maxProjectId = pj_pg_sub_jpaRepository.findMaxProjectId(wssProjectCode);
            int nextProjectId = (maxProjectId != 0) ? maxProjectId + 1 : 1;

            // 8. 이슈 상세 내용 + 링크 구성
            String issueLink = "https://markany.atlassian.net/jira/core/projects/" +
                    프로젝트.getKey() +
                    "/board?groupBy=status&selectedIssue=" +
                    getTrimmedJiraKey(이슈.get지라_이슈_키());

            String 상세내용 = String.format("%s<br><br>%s<br><br>%s",
                    safe(이슈.get지라_이슈_제목()),
                    safe(이슈.get상세내용()),
                    issueLink
            );

            // 9. 중복 체크
            Date 등록일 = 이슈.getCreateDate() != null ? 이슈.getCreateDate() : new Date();
            if (pj_pg_sub_jpaRepository.existsByCreationDateAndIssueContent(등록일, 상세내용)) {
                logger.info(" 중복된 이슈입니다. ====> 이슈 키: " + 이슈.get지라_이슈_키());
                return null;
            }

            // 10. 이슈 엔티티 생성 및 저장
            PJ_PG_SUB_Entity 이슈_엔티티 = PJ_PG_SUB_Entity.builder()
                    .projectId(String.valueOf(nextProjectId))
                    .projectCode(wssProjectCode)
                    .creationDate(이슈.getCreateDate() != null ? 이슈.getCreateDate() : new Date())
                    .writer(이슈.get담당자() != null ? 이슈.get담당자() : "담당자 없음")
                    .issueContent(상세내용)
                    .subNoticeFlag(false)
                    .issueMigrateFlag(true)
                    .build();

            return pj_pg_sub_jpaRepository.save(이슈_엔티티);

        } catch (Exception e) {
            logger.error("이슈 저장 중 오류"); // 로그 남기기
            throw new Exception("이슈 저장 중 오류 발생");
        }
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    public String getTrimmedJiraKey(String jiraKey) {
        if (jiraKey == null || jiraKey.isEmpty()) return jiraKey;

        String[] parts = jiraKey.split("-");
        if (parts.length >= 2) {
            return parts[0] + "-" + parts[1];
        } else {
            return jiraKey;
        }
    }

    private TB_PJT_BASE_Entity saveWssProjectInfo(TB_JML_Entity 프로젝트) throws Exception {
        if (프로젝트 == null || 프로젝트.getKey() == null || 프로젝트.getFlag() == null) {
            return null;
        }

        String wssProjectCode = validateProjectCode(프로젝트);
        String jiraProjectKey = 프로젝트.getKey();
        String flag = 프로젝트.getFlag();
        String projectName = 프로젝트.getJiraProjectName();
        String projectLeader = 프로젝트.getJiraProjectLeader();
        String salesManager = 프로젝트.getJiraProjectSalesManager();

        LocalDateTime localDateTime = 프로젝트.getMigratedDate();
        LocalDate localDate = localDateTime.toLocalDate();
        Date createdDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        if ("M".equals(flag)) {
            return backup_baseinfo_m_jpaRepository.findById(jiraProjectKey)
                    .map(유지보수정보 -> {
                        try {
                            logger.info("유지보수 정보 저장: " + 프로젝트.getKey());
                            return saveMaintenanceProject(wssProjectCode, projectName, createdDate, 유지보수정보, projectLeader, salesManager);
                        } catch (Exception e) {
                            throw new RuntimeException("유지보수 프로젝트 저장 중 오류 발생 :: 지라 프로젝트 키 : "+ jiraProjectKey, e);
                        }
                    })
                    .orElseGet(() -> {
                        try {
                            logger.info("유지보수 정보 저장: " + 프로젝트.getKey());
                            logger.info("유지보수 정보를 찾을 수 없습니다: " + jiraProjectKey);
                            return saveMaintenanceProjectWidhoutBaseInfo(wssProjectCode,createdDate, 프로젝트);
                        } catch (Exception e) {
                            throw new RuntimeException("유지보수 프로젝트 저장 중 오류 발생 :: 지라 프로젝트 키 : "+ jiraProjectKey, e);
                        }
                    });
        } else if ("P".equals(flag)) {
            return backup_baseinfo_p_jpaRepository.findById(jiraProjectKey)
                    .map(프로젝트정보 -> {
                        try {
                            logger.info("프로젝트 정보 저장: " + 프로젝트.getKey());
                            return saveProject(wssProjectCode, projectName,createdDate, 프로젝트정보, projectLeader, salesManager);
                        } catch (Exception e) {
                            throw new RuntimeException("프로젝트 저장 중 오류 발생 :: 지라 프로젝트 키 : "+ jiraProjectKey, e);
                        }
                    })
                    .orElseGet(() -> {
                        try {
                            logger.info("프로젝트 정보 저장: " + 프로젝트.getKey());
                            logger.info("프로젝트 정보를 찾을 수 없습니다: " + jiraProjectKey);
                            return saveProjectWithOutBaseInfo(wssProjectCode, createdDate,프로젝트);
                        } catch (Exception e) {
                            throw new RuntimeException("프로젝트 저장 중 오류 발생 :: 지라 프로젝트 키 : "+ jiraProjectKey, e);
                        }
                    });
        }

        return null;
    }

    private String validateProjectCode(TB_JML_Entity 프로젝트) {
        String projectCode = 프로젝트.getProjectCode();

        // null 또는 빈 문자열 또는 공백 문자열 거르기
        if (projectCode == null || projectCode.trim().isEmpty()) {
            return 프로젝트.getKey();
        }

        // 첫 글자 가져오기 (공백 제거 후)
        char firstChar = projectCode.trim().charAt(0);

        // 첫 글자가 영어(A-Z, a-z) 또는 숫자(0-9)가 아니거나 한글(가-힣)이면 거르기
        if (!Character.isLetterOrDigit(firstChar) || isKorean(firstChar)) {
            return 프로젝트.getKey();
        }

        return projectCode;
    }

    // 한글인지 확인하는 함수
    private boolean isKorean(char c) {
        return (c >= '가' && c <= '힣'); // 한글 범위 검사
    }


    private TB_PJT_BASE_Entity saveMaintenanceProject(String projectCode, String projectName, Date createdDate,
                                                      BACKUP_BASEINFO_M_Entity 유지보수정보, String projectLeader, String salesManager) throws Exception {
        if (유지보수정보 == null) return null;
        try {
            TB_PJT_BASE_Entity 유지보수_엔티티 = TB_PJT_BASE_Entity.builder()
                    .projectCode(projectCode.trim())
                    .projectFlag("M")
                    .projectName(projectName)
                    .projectNameDev(projectName)
                    .createdDate(createdDate)
                    .client(Objects.requireNonNullElse(유지보수정보.get고객사(), "고객사 정보 없음"))
                    .contractor(Objects.requireNonNullElse(유지보수정보.get계약사(), "계약사 정보 없음"))
                    .salesManager(Objects.requireNonNullElse(salesManager, "영업담당자 없음"))
                    .assignedEngineer(Objects.requireNonNullElse(projectLeader, "프로젝트 담당자 없음"))
                    .contract("계약".equals(유지보수정보.get계약_여부()) ? "1" : "0")
                    .inspectionType(
                            (유지보수정보.get점검_방법() != null ? 유지보수정보.get점검_방법() : "") +  (유지보수정보.get점검_주기() != null ? " " + 유지보수정보.get점검_주기() : "")
                    )
                    .contractStartDate(유지보수정보.get유지보수_시작일() != null ? 유지보수정보.get유지보수_시작일() : new Date())
                    .contractEndDate(유지보수정보.get유지보수_종료일() != null ? 유지보수정보.get유지보수_종료일() :  new Date())
                    .migrateFlag(true)
                    .issueMigrateFlag(true)
                    .build();

            return tb_pjt_base_jpaRepository.save(유지보수_엔티티);
        }catch (Exception e){
            throw new Exception("유지보수 프로젝트 저장 중 오류 발생 "+e.getMessage());
        }
    }

    private TB_PJT_BASE_Entity saveMaintenanceProjectWidhoutBaseInfo(String projectCode,Date createdDate,TB_JML_Entity 프로젝트) throws Exception{
        try {
            TB_PJT_BASE_Entity 유지보수_엔티티 = TB_PJT_BASE_Entity.builder()
                    .projectCode(projectCode.trim())
                    .projectFlag("M")
                    .projectName(프로젝트.getJiraProjectName())
                    .projectNameDev(프로젝트.getJiraProjectName())
                    .createdDate(createdDate)
                    .client("계약사 정보 없음")
                    .contractor("")
                    .salesManager(null)
                    .assignedEngineer(프로젝트.getJiraProjectLeader())
                    .contract("1")
                    .inspectionType("")
                    .migrateFlag(true)
                    .issueMigrateFlag(true)
                    .build();

            return tb_pjt_base_jpaRepository.save(유지보수_엔티티);
        }catch (Exception e){
            throw new Exception("유지보수 프로젝트 저장 중 오류 발생 "+e.getMessage());
        }
    }

    private TB_PJT_BASE_Entity saveProject(String projectCode, String projectName, Date createdDate , BACKUP_BASEINFO_P_Entity 프로젝트정보,
                                           String projectLeader, String salesManager) throws Exception{

        if (프로젝트정보 == null) return null;

        try {
            TB_PJT_BASE_Entity 프로젝트_엔티티 = TB_PJT_BASE_Entity.builder()
                    .projectCode(projectCode.trim())
                    .projectFlag("P")
                    .projectName(projectName)
                    .projectNameDev(projectName)
                    .createdDate(프로젝트정보.get프로젝트_배정일() != null ? 프로젝트정보.get프로젝트_배정일() : new Date())
                    .client(Objects.requireNonNullElse(프로젝트정보.get고객사(), "고객사 정보 없음"))
                    .contractor(Objects.requireNonNullElse(프로젝트정보.get계약사(), "계약사 정보 없음"))
                    .salesManager(Objects.requireNonNullElse(salesManager, "영업담당자 정보 없음"))
                    .assignedEngineer(Objects.requireNonNullElse(projectLeader, "프로젝트 담당자 정보 없음"))
                    .migrateFlag(true)
                    .issueMigrateFlag(true)
                    .build();

            return tb_pjt_base_jpaRepository.save(프로젝트_엔티티);

        }catch (Exception e){
            throw new Exception("프로젝트 저장 중 오류 발생"+e.getMessage());
        }
    }

    private TB_PJT_BASE_Entity saveProjectWithOutBaseInfo(String projectCode,Date createdDate ,TB_JML_Entity 프로젝트) throws Exception{

        try {
            TB_PJT_BASE_Entity 프로젝트_엔티티 = TB_PJT_BASE_Entity.builder()
                    .projectCode(projectCode.trim())
                    .projectFlag("P")
                    .projectName(프로젝트.getJiraProjectName())
                    .projectNameDev(프로젝트.getJiraProjectName())
                    .createdDate(createdDate)
                    .client("고객사 정보 없음")
                    .contractor("계약사 정보 없음")
                    .salesManager("영업담당자 정보 없음")
                    .assignedEngineer(프로젝트.getJiraProjectLeader())
                    .migrateFlag(true)
                    .issueMigrateFlag(true)
                    .build();

            return tb_pjt_base_jpaRepository.save(프로젝트_엔티티);

        }catch (Exception e){
            throw new Exception("프로젝트 저장 중 오류 발생"+e.getMessage());
        }
    }

}


