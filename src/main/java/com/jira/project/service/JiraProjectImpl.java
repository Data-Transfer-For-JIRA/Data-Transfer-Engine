package com.jira.project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jira.account.service.Account;
import com.jira.issue.service.JiraIssue;
import com.jira.project.model.dao.TB_JLL_JpaRepository;
import com.jira.project.model.dao.TB_JML_JpaRepository;
import com.jira.project.model.dto.CreateProjectDTO;
import com.jira.project.model.dto.CreateProjectResponseDTO;
import com.jira.project.model.dto.ProjectDTO;
import com.jira.project.model.entity.TB_JLL_Entity;
import com.jira.project.model.entity.TB_JML_Entity;
import com.jira.project.model.entity.TB_PJT_BASE_Entity;
import com.utils.ProjectConfig;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service("jiraProjcet")
public class JiraProjectImpl implements JiraProject {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private  ProjectConfig projectConfig;

    @Autowired
    private com.jira.project.model.dao.TB_PJT_BASE_JpaRepository TB_PJT_BASE_JpaRepository;

    @Autowired
    private TB_JML_JpaRepository TB_JML_JpaRepository;

    @Autowired
    private TB_JLL_JpaRepository TB_JLL_JpaRepository;

    @Autowired
    private WebClientUtils webClientUtils;

    @Autowired
    private JiraIssue jiraIssue;

    @Autowired
    private Account account;

    /*
    *  특정 키워드로 디비에서 지라 프로젝트 목록 조회
    * */
    @Override
    @Transactional
    public List<TB_JML_Entity> getJiraProjectListBySearchKeywordOnJML(String searchKeyword ) throws Exception{
        List<TB_JML_Entity> searchResult = TB_JML_JpaRepository.findByKeyOrProjectCodeOrWssProjectNameContaining(searchKeyword);
        return searchResult;
    }

    @Override
    @Transactional
    public Map<String, String> createProjectFromDB(String projectCode) throws Exception{
        logger.info("프로젝트 생성 시작");

        Map<String, String> result = new HashMap<>();
        try {
            // 프로젝트 조회
            Optional<TB_PJT_BASE_Entity> table_info = TB_PJT_BASE_JpaRepository.findById(projectCode);
            if (table_info.isPresent()) { // 프로젝트 조회 성공
                logger.info("WSS 프로젝트 조회");
                String flag = table_info.get().getProjectFlag();
                String projectName = table_info.get().getProjectName();
                String projectKey = namingJiraKey();
                boolean migrateFlag = table_info.get().getMigrateFlag();
                String assignees = table_info.get().getAssignedEngineer();
                if (!migrateFlag) {
                    // 프로젝트 정보 Setting
                    CreateProjectDTO projectInfo = requiredData(flag,projectName, projectKey);
                    // 프로젝트 생성
                    CreateProjectResponseDTO Response = createJiraProject(projectInfo);

                    saveSuccessData(Response.getProjectKey() , Response.getProjectId(),projectCode,projectName,projectInfo.getName(),flag,assignees); // 템플릿을 통한 생성 방법
                    // 디비 이관 flag 변경
                    checkMigrateFlag(projectCode);

                    result.put("이관 성공",projectCode );
                    return result;
                }else{
                    result.put("이미 이관한 프로젝트",projectCode );
                    return result;
                }

            } else { //프로젝트 조회 실패

                result.put("프로젝트 조회 실패", projectCode);
                return result;
            }
        }catch (Exception e){ // 프로젝트 이관 실패 시

            result.put("이관 실패", projectCode);
            logger.error(e.getMessage());

        }

        return result;
    }

    public CreateProjectResponseDTO createJiraProject( CreateProjectDTO createProjectDTO) throws Exception{
        logger.info("JIRA 프로젝트 생성 시작");

        // 템플릿을 통한 생성 방법
        String endpoint = "/rest/simplified/latest/project/shared";

        CreateProjectResponseDTO Response = webClientUtils.post(endpoint, createProjectDTO, CreateProjectResponseDTO.class).block();
        return Response;

    }
    // 프로젝트 기본 생성 방법
    public CreateProjectDTO requiredData(String flag , String projectName, String projectKey) throws Exception{
        logger.info("JIRA 프로젝트 생성시 필요 데이터 조합");
        CreateProjectDTO projectInfo = new CreateProjectDTO(); // 프로젝트 생성 필수 정보

        // 템플릿을 통한 생성 방법
        projectInfo.setKey(projectKey);

        String jiraProjectName;

        if (flag.equals("P")) { //프로젝트
            jiraProjectName = projectConfig.projectHeaderName + projectName;
            projectInfo.setName(jiraProjectName);
            projectInfo.setExistingProjectId(projectConfig.projectTemplate);

        } else { // 유지보수
            jiraProjectName = projectConfig.maintenanceHeaderName + projectName;
            projectInfo.setName(jiraProjectName);
            projectInfo.setExistingProjectId(projectConfig.maintenanceTemplate);
        }
        return projectInfo;
    }


    /*
     * 리펙토링 필요 2023 12 02
     * - 중복 제거 하였지만 효율서이 떨어짐
     * */
    public String namingJiraKey() throws Exception {
        logger.info("JIRA 프로젝트 키 생성");
        String jiraKey;
        long count = TB_JML_JpaRepository.count();
        if (count == 0) { // 최초
            return "ED1";
        } else {
            String recentKey = TB_JML_JpaRepository.findTopByOrderByMigratedDateDesc().getKey();

            int num = Integer.parseInt(recentKey.substring(projectConfig.projectKeyNum));

            while (true) {
                num++;
                //개발
                jiraKey = projectConfig.keyHeader + num;
                //운영
                //jiraKey = "ED" + num;
                if (checkValidationJiraKey(jiraKey)) {
                    return jiraKey;
                }
            }
        }
    }

    public Boolean checkValidationJiraKey(String key) throws Exception{
        logger.info("JIRA 프로젝트 키 유효성 체크 ");
        try {
            String endpoint = "/rest/api/3/project/"+key;
            webClientUtils.get(endpoint, JsonNode.class).block();
            logger.info("[::TransferProjectImpl::] checkValidationJiraKey -> " + "존재하는 프로젝트");
            return false; // 있는 프로젝트
        }catch (Exception e){
            logger.info("[::TransferProjectImpl::] checkValidationJiraKey -> " + "존재하지않는 프로젝트");
            return true; // 없는 프로젝트
        }
    }

    public void saveSuccessData(String key, String id , String projectCode , String projectName , String jiraProjectName, String flag , String projectAssignees) throws Exception{

        logger.info("JIRA 프로젝트 생성 결과 저장");
        TB_JML_Entity save_success_data  = new TB_JML_Entity();
        save_success_data.setKey(key);
        save_success_data.setId(id);
        save_success_data.setProjectCode(projectCode);
        save_success_data.setWssProjectName(projectName);
        save_success_data.setJiraProjectName(jiraProjectName);
        save_success_data.setFlag(flag);
        save_success_data.setProjectAssignees(projectAssignees);
        TB_JML_JpaRepository.save(save_success_data);
    }
    public void checkMigrateFlag(String projectCode){
        logger.info("이관 여부 체크");
        TB_PJT_BASE_Entity entity =  TB_PJT_BASE_JpaRepository.findById(projectCode).orElseThrow(() -> new NoSuchElementException("프로젝트 코드 조회에 실패하였습니다.: " + projectCode));
        entity.setMigrateFlag(true);
    }

    @Override
    public ProjectDTO reassignProjectLeader(String jiraProjectCode, String assignee) throws Exception{
        logger.info("[::reassignProjectLeader::]  프로젝트 담당자 지정 변경");
        logger.info("[::reassignProjectLeader::]  assignee -> " + assignee);
        try {
            String endpoint ="/rest/api/3/project/"+jiraProjectCode;

            CreateProjectDTO createProjectDTO = new CreateProjectDTO();
            createProjectDTO.setAssigneeType("PROJECT_LEAD");
            String assigneeId = null;
            if(Character.isDigit(assignee.charAt(0))) { // 숫자로 시작하면
                assigneeId = assignee;
            } else if(Character.isLetter(assignee.charAt(0))) {// 문자로 시작하면
                assigneeId =  jiraIssue.getOneAssigneeId(assignee);
            }
            createProjectDTO.setLeadAccountId(assigneeId);

            ProjectDTO returnDate = webClientUtils.put(endpoint, createProjectDTO, ProjectDTO.class).block();

            return returnDate;
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public ProjectDTO getJiraProjectInfoByJiraKey(String jiraKey) throws Exception{
        logger.info("[::TransferProjectImpl::]  지라 프로젝트 조회 -> "+jiraKey);
        String endpoint ="/rest/api/3/project/"+jiraKey;

        ProjectDTO result = webClientUtils.get(endpoint,ProjectDTO.class).block();

        return result;
    }
    @Override
    @Transactional
    public List<TB_JLL_Entity> saveProjectsRelation() throws Exception{
        logger.info("[::TransferProjectImpl::] 연결된 프로젝트 목록 저장 메서드");
        List<TB_PJT_BASE_Entity> relatedProjectIsNotNull = TB_PJT_BASE_JpaRepository.findNonNullAndNonEmptyRelatedProjects(); // 널값과 빈 값이 있는 데이터는 제외하고 조회
        Map<String, List<String>> result = mappingRelatedProject(relatedProjectIsNotNull);
        saveRelationsOnDB(result);

        return TB_JLL_JpaRepository.findAll();
    }

    public Map<String, List<String>> mappingRelatedProject(List<TB_PJT_BASE_Entity> relatedProjectIsNotNull ) throws Exception{
        logger.info("[::TransferProjectImpl::] 연결된 프로젝트 목록 조회");
        // 프로젝트 코드와 Jira 키를 매핑하는 Map을 생성
        Map<String, String> projectCodeToJiraKey = TB_JML_JpaRepository.findAll().stream()
                .collect(Collectors.toMap(TB_JML_Entity::getProjectCode, TB_JML_Entity::getKey));

        Map<String, List<String>> relatedProjectMappingList = new HashMap<>();
        for(TB_PJT_BASE_Entity project : relatedProjectIsNotNull) {
            String projectCode = project.getRelatedProject();
            String jiraKey = projectCodeToJiraKey.get(projectCode);

            if (jiraKey != null) {  // jiraKey가 null이 아닌 경우에만 처리
                List<String> linkedProjectList = TB_PJT_BASE_JpaRepository.findProjectCodesByRelatedProject(projectCode);
                List<String> jiraLinkedProjectKey = new ArrayList<>();
                for(String linkedProject : linkedProjectList){
                    // 미리 생성해둔 Map을 이용해 Jira 키를 찾음
                    String JiraLinkedProject = projectCodeToJiraKey.get(linkedProject);
                    jiraLinkedProjectKey.add(JiraLinkedProject);
                }
                relatedProjectMappingList.put(jiraKey, jiraLinkedProjectKey);
            }
        }

        // 프로젝트 - 연관된 프로젝트로 구조화
        System.out.println(relatedProjectMappingList);
        return relatedProjectMappingList;
    }

    public void saveRelationsOnDB(Map<String, List<String>> resultMap) throws Exception{
        resultMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(value -> new AbstractMap.SimpleEntry<>(entry.getKey(), value)))
                .forEach(entry -> saveForJLL(entry.getKey(),entry.getValue()));
    }

    public TB_JLL_Entity saveForJLL(String parentKey, String childKey){
       TB_JLL_Entity entity = TB_JLL_Entity.builder().parentKey(parentKey).childKey(childKey).build();
       return TB_JLL_JpaRepository.save(entity);
    }

    @Override
    public List<Map<String, String>> deleteJiraProject(List<String> jiraProjectCodes) {

        logger.info("[::TransferProjectImpl::] 지라 프로젝트 삭제");

        List<Map<String, String>> resultList = new ArrayList<>();

        String baseEndpoint = "/rest/api/3/project/";
        for (String jiraProjectCode : jiraProjectCodes) {
            String endpoint = baseEndpoint + jiraProjectCode;

            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("jiraProjectCode", jiraProjectCode);

            // 지라에서 삭제
            try {
                Optional<Boolean> response = webClientUtils.executeDelete(endpoint);

                if (response.isPresent()) {
                    resultMap.put("result1", "[지라] 프로젝트 삭제 성공");
                } else {
                    resultMap.put("result1", "[지라] 존재하지 않는 프로젝트 또는 이미 삭제된 프로젝트");
                }
            } catch (Exception e) {
                resultMap.put("result1", "[지라] 존재하지 않는 프로젝트 또는 이미 삭제된 프로젝트");
            }

            // DB에서 삭제
            Optional<TB_JML_Entity> jmlEntity = Optional.ofNullable(TB_JML_JpaRepository.findByKey(jiraProjectCode));
            if (jmlEntity.isPresent()) {
                TB_JML_JpaRepository.delete(jmlEntity.get());
                resultMap.put("result2", "[DB] 프로젝트 삭제 성공");
            } else {
                resultMap.put("result2", "[DB] 존재하지 않는 프로젝트 또는 이미 삭제된 프로젝트");
            }

            resultList.add(resultMap);
        }

        return resultList;
    }

    @Override
    @Transactional
    public ProjectDTO updateProjectInfo(CreateProjectDTO createProjectDTO) throws Exception{

        logger.info("[::TransferProjectImpl::] 지라 프로젝트 업데이트");

        String endpoint = "/rest/api/3/project/"+createProjectDTO.getKey();

        CreateProjectDTO 업데이트_데이터 = new CreateProjectDTO();
            
            // 프로젝트 이름 수정
        if(createProjectDTO.getName() != null && !createProjectDTO.getName().isEmpty()){
            업데이트_데이터.setName(createProjectDTO.getName());
        }

            // 담당자 수정
        if(createProjectDTO.getLeadAccountId() !=  null && !createProjectDTO.getLeadAccountId().isEmpty()){
            업데이트_데이터.setLeadAccountId(createProjectDTO.getLeadAccountId());
        }

        ProjectDTO  결과 = webClientUtils.put(endpoint,업데이트_데이터, ProjectDTO.class).block();

        if(결과.getKey() != null){ // JML 테이블 업데이트
            String 이름;

            TB_JML_Entity 업데이트_대상 = TB_JML_JpaRepository.findByKey(결과.getKey());
            업데이트_대상.setJiraProjectName(결과.getName());

            String 담당자_이름 = 결과.getLead().getDisplayName();

            if(담당자_이름.contains("(")){
                int startIndex = 담당자_이름.indexOf("(");
                이름= 담당자_이름.substring(0, startIndex).trim();
            }else{
                이름 = 담당자_이름; // epage dev 케이스
            }

            업데이트_대상.setJiraProjectLeader(이름);

            TB_JML_JpaRepository.save(업데이트_대상);
        }

        return 결과;
    }

}
