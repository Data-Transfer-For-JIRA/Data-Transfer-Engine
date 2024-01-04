package com.platform.service;

import com.account.dto.AdminInfoDTO;
import com.account.service.Account;
import com.platform.dto.BaseDTO;
import com.transfer.project.model.dto.CreateProjectDTO;
import com.transfer.project.model.dto.CreateProjectResponseDTO;
import com.transfer.project.service.TransferProject;
import com.transfer.project.service.TransferProjectImpl;
import com.utils.ProjectConfig;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Service("platformProject")
public class PlatformProjectImpl implements PlatformProject {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TransferProjectImpl transferProject;

    @Autowired
    private ProjectConfig projectConfig;

    @Autowired
    private Account account;

    @Override
    public Map<String, String> platformCreateProject(BaseDTO baseDTO) throws Exception {

        BaseDTO.EssentialDTO essentialDTO = baseDTO.getEssential();
        BaseDTO.CommonDTO commonDTO = baseDTO.getCommon();
        BaseDTO.SelectedDTO selectedDTO = baseDTO.getSelected();

        Map<String, String> result = new HashMap<>();

        CreateProjectDTO createProjectDTO = new CreateProjectDTO();

        String jiraProjectKey = transferProject.NamingJiraKey();
        String jiraProjectName = "";

        // 필수
        String projectFlag = essentialDTO.getProjectFlag();
        String projectName = essentialDTO.getProjectName();

        // 선택
        String projectCode = commonDTO.getProjectCode();
        String assignee = commonDTO.getAssignee();
        String subAssignee = commonDTO.getSubAssignee();

        // 담당자 설정
        String assignees = "";
        if (assignee != null && !assignee.trim().isEmpty()) {
            assignees = assignee;
        }
        if (subAssignee != null && !subAssignee.trim().isEmpty()) {
            if (!assignees.isEmpty()) {
                assignees += ",";
            }
            assignees += subAssignee;
        }

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
        String finalJiraProjectName = jiraProjectName;
        String finalAssignees = assignees;

        result.put("jiraProjectCode", jiraProjectKey);
        result.put("jiraProjectName", finalJiraProjectName);
        try {

            logger.info("[::platformCreateProject::] dto 확인 " + createProjectDTO.toString());
            CreateProjectResponseDTO response = WebClientUtils.post(webClient, endpoint, createProjectDTO, CreateProjectResponseDTO.class)
                    .doOnSuccess(res -> {
                        // 성공적으로 응답을 받았을 때
                        result.put("result", "프로젝트 생성 성공");

                        try {
                            transferProject.saveSuccessData(jiraProjectKey, res.getProjectId(), projectCode, projectName, finalJiraProjectName, projectFlag, finalAssignees); // DB 저장
                            logger.error("[::platformCreateProject::] DB 저장 완료");
                        } catch (Exception e) {
                            logger.error("[::platformCreateProject::] DB 저장 실패");
                        }
                        if (finalAssignees != null && !finalAssignees.trim().isEmpty()){
                            try {
                                String leader = Arrays.stream(finalAssignees.split(",")).findFirst().orElse(finalAssignees);
                                transferProject.reassignProjectLeader(res.getProjectKey(),leader);
                                logger.info("[::platformCreateProject::] 프로젝트 담당자 지정 성공");
                            } catch (Exception e) {
                                logger.error("[::platformCreateProject::] 프로젝트 담당자 지정 실패");
                            }
                        }
                    })
                    .doOnError(e -> {
                        // 에러 처리
                        logger.info("[::platformCreateProject::] error 발생");
                        result.put("result", "프로젝트 생성 실패");
                    })
                    .block();

            logger.info("[::platformCreateProject::] response -> " + response.toString());
            return result;

        } catch (Exception ex) {
            logger.info("[::platformCreateProject::] try-catch");
            result.put("result", "프로젝트 생성 실패");
            return result;
        }
    }
}
