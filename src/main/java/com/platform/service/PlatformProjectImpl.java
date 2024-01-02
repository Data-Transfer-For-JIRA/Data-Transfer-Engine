package com.platform.service;

import com.account.dto.AdminInfoDTO;
import com.account.service.Account;
import com.platform.dto.AssignProjectDTO;
import com.platform.dto.BaseDTO;
import com.transfer.issue.model.dto.ResponseIssueDTO;
import com.transfer.issue.service.TransferIssue;
import com.transfer.project.model.dto.CreateProjectDTO;
import com.transfer.project.model.dto.CreateProjectResponseDTO;
import com.transfer.project.service.TransferProjectImpl;
import com.utils.ProjectConfig;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Service("platformProject")
public class PlatformProjectImpl implements PlatformProject {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private TransferProjectImpl transferProject;

    @Autowired
    private TransferIssue transferIssue;

    @Autowired
    private ProjectConfig projectConfig;

    @Autowired
    private Account account;

    @Override
    public Map<String, String> platformCreateProject(BaseDTO baseDTO) throws Exception {

        Map<String, String> result = new HashMap<>();

        CreateProjectDTO createProjectDTO = new CreateProjectDTO();

        String jiraProjectKey = transferProject.NamingJiraKey();
        String projectFlag = baseDTO.getProjectFlag();
        String projectCode = baseDTO.getProjectCode();
        String projectName = baseDTO.getProjectName();
        String jiraProjectName = "";

        if (projectFlag.equals("P")) { // 프로젝트
            jiraProjectName = "ED-P_" + projectName;
            createProjectDTO.setName(jiraProjectName);
            createProjectDTO.setExistingProjectId(projectConfig.projectTemplate);
        } else { // 유지보수
            jiraProjectName = "ED-M_" + projectName;
            createProjectDTO.setName(jiraProjectName);
            createProjectDTO.setExistingProjectId(projectConfig.maintenanceTemplate);
        }
        createProjectDTO.setKey(jiraProjectKey);

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());

        // 템플릿으로 프로젝트 생성
        // TODO: 프로젝트 코드는 필수 필드 아니므로 처리 필요, BaseDTO 구조 정리 필요
        String endpoint = "/rest/simplified/latest/project/shared";
        String finalJiraProjectName = jiraProjectName;

        result.put("jiraProjectCode", jiraProjectKey);
        result.put("jiraProjectName", finalJiraProjectName);
        try {
            CreateProjectResponseDTO response = WebClientUtils.post(webClient, endpoint, createProjectDTO, CreateProjectResponseDTO.class)
                    .doOnSuccess(res -> {
                        // 성공적으로 응답을 받았을 때
                        result.put("result", "프로젝트 생성 성공");

                        try {
                            transferProject.saveSuccessData(jiraProjectKey, res.getProjectId(), projectCode, projectName, finalJiraProjectName, projectFlag, null); // DB 저장
                        } catch (Exception e) {
                            logger.error("[::platformCreateProject::] DB 저장 실패");
                        }
                    })
                    .doOnError(e -> {
                        // 에러 처리
                        result.put("result", "프로젝트 생성 실패");
                    })
                    .block();

            System.out.println("[::platformCreateProject::] response -> " + response.toString());
            return result;

        } catch (Exception ex) {
            result.put("result", "프로젝트 생성 실패");
            return result;
        }
    }
    /*
    *  API를 통해 생성된 프로젝트의 담당자를 변경하는 메서드
    * */
    @Override
    public Map<String,String> platformAssignProject(AssignProjectDTO assignProjectDTO) throws Exception{
        String jiraProjectCode = assignProjectDTO.getJiraProjectCode();
        //String assignee    = assignProjectDTO.getAssignee();
        //String assigneeId = transferIssue.getOneAssigneeId(assignee);
        String assigneeId = assignProjectDTO.getAssigneeId();

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint ="/rest/api/3/project/"+jiraProjectCode;

        CreateProjectDTO createProjectDTO = new CreateProjectDTO();
        createProjectDTO.setLeadAccountId(assigneeId);
        createProjectDTO.setAssigneeType("PROJECT_LEAD");

        String result = WebClientUtils.put(webClient, endpoint, createProjectDTO, String.class).block();

        return null;
    }

    /*
     *  메서드 통해 생성된 프로젝트의 담당자를 변경하는 메서드
     * */
    public Map<String,String> platformAssignProjectByMethod(String jiraProjectCode, String assigneeId) throws Exception{

        AdminInfoDTO info = account.getAdminInfo(1);
        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint ="/rest/api/3/project/"+jiraProjectCode;

        CreateProjectDTO createProjectDTO = new CreateProjectDTO();
        createProjectDTO.setLeadAccountId(assigneeId);
        createProjectDTO.setAssigneeType("PROJECT_LEAD");

        String result = WebClientUtils.put(webClient, endpoint, createProjectDTO, String.class).block();

        return null;
    }
}
