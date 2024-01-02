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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@AllArgsConstructor
@Service("platformProject")
public class PlatformProjectImpl implements PlatformProject {

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
        String endpoint = "/rest/simplified/latest/project/shared";
        CreateProjectResponseDTO response = WebClientUtils.post(webClient, endpoint, createProjectDTO, CreateProjectResponseDTO.class).block();

        // DB 저장
        transferProject.saveSuccessData(jiraProjectKey, response.getProjectId(), projectCode, projectName, jiraProjectName, projectFlag,null);

        System.out.println("response: " + response.toString());

        return null;
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
