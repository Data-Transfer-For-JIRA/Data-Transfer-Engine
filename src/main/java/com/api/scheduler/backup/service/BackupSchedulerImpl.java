package com.api.scheduler.backup.service;

import com.jira.issue.model.dto.FieldDTO;
import com.jira.issue.model.dto.search.SearchIssueDTO;
import com.jira.issue.model.dto.search.SearchMaintenanceInfoDTO;
import com.jira.issue.model.dto.search.SearchProjectInfoDTO;
import com.jira.issue.model.dto.weblink.SearchWebLinkDTO;
import com.jira.issue.model.entity.backup.BACKUP_BASEINFO_M_Entity;
import com.jira.issue.model.entity.backup.BACKUP_BASEINFO_P_Entity;
import com.jira.issue.service.JiraIssue;
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
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service("backupScheduler")
public class BackupSchedulerImpl implements BackupScheduler {

    @Autowired
    JiraProject jiraProject;

    @Autowired
    JiraIssue JiraIssue;

    @Autowired
    private TB_JML_JpaRepository TB_JML_JpaRepository;

    @Autowired
    private com.jira.issue.model.dao.BACKUP_BASEINFO_M_JpaRepository BACKUP_BASEINFO_M_JpaRepository;

    @Autowired
    private com.jira.issue.model.dao.BACKUP_BASEINFO_P_JpaRepository BACKUP_BASEINFO_P_JpaRepository;

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

    /*
    *  프로젝트 백업은 프로젝트 이름, 담당자 정보, 업데이트 시간 정보를 백업 및 업데이트 진행
    * */
    @Override
    @Transactional
    public void 지라프로젝트_백업() throws Exception{
        List<TB_JML_Entity> 모든_프로젝트 = TB_JML_JpaRepository.findAll(); // 전체 프로젝트 조회

        for(TB_JML_Entity 프로젝트 : 모든_프로젝트){

            String 지라_프로제트_키 = 프로젝트.getKey(); // 지라 키 조회

            지라프로젝트_JML테이블_업데이트(지라_프로제트_키, 프로젝트); // 해당 프로젝트 지라에서 조회 후 업데이트 처리

        }

    }
    private void 지라프로젝트_JML테이블_업데이트(String 지라_프로제트_키, TB_JML_Entity 프로젝트) throws Exception{
        Date currentTime = new Date();
        String message ="["+지라_프로제트_키+"] - "+ currentTime+" - ";

        try {

            String 저장된_담당자 = 프로젝트.getJiraProjectLeader();  // 디비에 저장된 담당자
            String 저장된_프로젝트_이름 = 프로젝트.getJiraProjectName(); // 디비에 저장된 프로젝트 이름

            ProjectDTO 조회한_프로제트_정보 = jiraProject.getJiraProjectInfoByJiraKey(지라_프로제트_키); // 지라에서 조회한 프로젝트 정보

            String 가공한_담당자_이름; // 지라에서 조회한 담당자 이름

            String 담당자_이름 = 조회한_프로제트_정보.getLead().getDisplayName();

            String 프로젝트_이름 = 조회한_프로제트_정보.getName();

            LocalDateTime 업데이트_시간 = LocalDateTime.now();

            if(담당자_이름.contains("(")){
                int startIndex = 담당자_이름.indexOf("(");
                가공한_담당자_이름= 담당자_이름.substring(0, startIndex).trim();
            }else{
                가공한_담당자_이름 = 담당자_이름; // epage dev 케이스
            }

            // 변경된 부분만 업데이트
            TB_JML_Entity 업데이트_정보 = new TB_JML_Entity();
            업데이트_정보.setKey(지라_프로제트_키);
            // 프로젝트 정보 업데이트
            if (!저장된_프로젝트_이름.equals(프로젝트_이름)) {
                업데이트_정보.setJiraProjectName(프로젝트_이름);
                message += " 프로젝트 이름이 "+저장된_프로젝트_이름 +" 에서"+프로젝트_이름 +"로 업데이트 되었습니다. \n";
            }
            // 담당자 정보 업데이트
            if (!저장된_담당자.equals(가공한_담당자_이름)) {
                업데이트_정보.setJiraProjectLeader(가공한_담당자_이름);
                message += " 프로젝트 담당자 정보가 "+저장된_담당자 +" 에서"+가공한_담당자_이름 +"로 업데이트 되었습니다. \n";
            }
            업데이트_정보.setUpdateDate(업데이트_시간);

            if (!저장된_프로젝트_이름.equals(프로젝트_이름) || !저장된_담당자.equals(가공한_담당자_이름)) { // 변경 사항 있을 때 업데이트
                TB_JML_JpaRepository.save(업데이트_정보);

                SaveLog.SchedulerResult("BACKUP\\PROJECT\\SUCCESS",message,currentTime);
            }

        }catch (Exception e){
            message += e.getMessage()+"프로젝트 정보 업데이트간 오류 발생";
            SaveLog.SchedulerResult("BACKUP\\PROJECT\\FAIL",message,currentTime);
            logger.error(message);
            throw new Exception(e.getMessage());
        }
    }


    /*
     *  기본정보 이슈 데이터를 백업
     * */
    @Override
    @Transactional
    public void 지라기본정보_백업() throws Exception{
        List<TB_JML_Entity> 모든_프로젝트 = TB_JML_JpaRepository.findAll(); // 전체 프로젝트 조회

        for(TB_JML_Entity 프로젝트 : 모든_프로젝트){

            String 지라_프로제트_키 = 프로젝트.getKey(); // 지라 키 조회
            String 프로젝트_유형 = 프로젝트.getFlag();

            지라_기본정보_업데이트(지라_프로제트_키,프로젝트_유형); // 해당 프로젝트 지라에서 조회 후 업데이트 처리
        }
    }

    public void 지라_기본정보_업데이트(String 지라_프로제트_키,String 프로젝트_유형) throws Exception{

        if(프로젝트_유형.equals("M")){ // 유지보수 기본 정보
            SearchIssueDTO<SearchMaintenanceInfoDTO> 유지보수_기본_정보 = JiraIssue.getMaintenanceIssue(지라_프로제트_키);

        }else{// 프로젝트 기본 정보
            SearchIssueDTO<SearchProjectInfoDTO> 프로젝트_기본_정보 = JiraIssue.getProjectIssue(지라_프로제트_키);

        }

    }

    /*
     *  기본정보 이슈 저장
     * */
    @Override
    @Transactional
    public Boolean 기본정보이슈_저장(String 지라_키,String 프로젝트_유형) throws Exception{
        try {
            if(프로젝트_유형.equals("M")){
                logger.info("유지보수 기본정보 이슈 저장 시작");
                SearchIssueDTO<SearchMaintenanceInfoDTO> 조회결과 = JiraIssue.getMaintenanceIssue(지라_키);
                if (조회결과 == null || 조회결과.getFields() == null) {
                    throw new Exception("유효한 조회 결과가 없습니다.");
                }
                유지보수_기본정보이슈_저장(조회결과);

                return true;
            }
            else{
                logger.info("프로젝트 기본정보 이슈 저장 시작");
                SearchIssueDTO<SearchProjectInfoDTO> 조회결과 = JiraIssue.getProjectIssue(지라_키);
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

    private void 유지보수_기본정보이슈_저장( SearchIssueDTO<SearchMaintenanceInfoDTO>  조회결과) throws Exception {
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

        String 연관된_프로젝트_키 =연관된_프로젝트_키_가져오기( JiraIssue.getWebLinkByJiraIssueKey(이슈_키) );

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
    }

    private void 프로젝트_기본정보이슈_저장(SearchIssueDTO<SearchProjectInfoDTO> 조회결과) throws Exception{
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

        String 연관된_프로젝트_키 =연관된_프로젝트_키_가져오기( JiraIssue.getWebLinkByJiraIssueKey(이슈_키) );

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
    }


    /*
     *  지라 이슈 데이터를 백업
     * */
    @Override
    @Transactional
    public void 지라이슈_백업() throws Exception{

    }
}
