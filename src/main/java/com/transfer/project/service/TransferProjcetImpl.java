package com.transfer.project.service;

import com.transfer.project.model.dao.TB_PJT_BASE_JpaRepository;
import com.transfer.project.model.dao.TB_JML_JpaRepository;
import com.transfer.project.model.dto.ProjectInfoData;

import com.transfer.project.model.entity.TB_JML_Entity;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;
import com.utils.HashString;
import com.utils.JiraConfig;
import com.transfer.project.model.dto.ProjectCreateDTO;
import com.utils.ProjectConfig;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Service("transferProjcet")
public class TransferProjcetImpl implements TransferProjcet{

    @Autowired
    private JiraConfig jiraConfig;

    @Autowired
    private  ProjectConfig projectConfig;

    @Autowired
    private TB_PJT_BASE_JpaRepository TB_PJT_BASE_JpaRepository;

    @Autowired
    private TB_JML_JpaRepository TB_JML_JpaRepository;

    @Autowired
    private TB_JML_Entity TB_JML_Entity;

    @Override
    public ProjectInfoData createProject(ProjectCreateDTO projectCreateDTO) throws Exception{

        WebClient webClient = WebClientUtils.createJiraWebClient(jiraConfig.baseUrl, jiraConfig.jiraID, jiraConfig.apiToken);
        String endpoint = "/rest/api/3/project";
        ProjectInfoData Response = WebClientUtils.post(webClient, endpoint, projectCreateDTO, ProjectInfoData.class).block();
        return Response;

    }


    @Override
    @Transactional
    public Page<TB_PJT_BASE_Entity> getDataBaseProjectData(int pageIndex, int pageSize) throws Exception{

        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        Page<TB_PJT_BASE_Entity> page = TB_PJT_BASE_JpaRepository.findAllByOrderByCreatedDateDesc(pageable);

        return page;
    }
    @Override
    @Transactional
    public Map<String, Boolean> CreateProjectFromDB(String projectCode) throws Exception {

        try {
            Optional<TB_PJT_BASE_Entity> table_info = TB_PJT_BASE_JpaRepository.findById(projectCode);
            if (table_info.isPresent()) {

                String flag = table_info.get().getProjectFlag();
                String projectName = table_info.get().getProjectName();
                String projectKey = HashString.hashing(projectCode);
                String jiraProjectName;

                ProjectCreateDTO projectInfo = new ProjectCreateDTO();

                projectInfo.setAssigneeType(projectConfig.assigneType);
                projectInfo.setCategoryId(projectConfig.categoryId);
                projectInfo.setKey(projectKey);
                projectInfo.setLeadAccountId(projectConfig.leadAccountId);
                projectInfo.setProjectTypeKey(projectConfig.projectTypeKey);

                if (flag.equals("P")) { //프로젝트
                    jiraProjectName = "전자문서_프로젝트_WSS_" + projectName + ")";
                    projectInfo.setName(projectName);

                } else { // 유지보수
                    jiraProjectName = "전자문서_유지보수_WSS_(" + projectName + ")";
                    projectInfo.setName(projectName);
                }

                ProjectInfoData Response = createProject(projectInfo);

                TB_JML_Entity save_success_data  = new TB_JML_Entity();
                save_success_data.setKey(Response.getKey());
                save_success_data.setJiraProjectName(jiraProjectName);
                save_success_data.setProjectCode(projectCode);
                save_success_data.setWssProjectName(projectName);

                TB_JML_JpaRepository.save(save_success_data);

                Map<String, Boolean> result = new HashMap<>();

                result.put("migration", true);

                return result;
            } else {
                Map<String, Boolean> project_find = new HashMap<>();

                project_find.put("project_find", false);

                return project_find;
            }
        }catch (Exception e){
            Map<String, Boolean> result = new HashMap<>();

            result.put("migration", false);

            return result;
        }



    }
}
