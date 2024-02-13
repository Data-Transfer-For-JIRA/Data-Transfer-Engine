package com.jira.project.controller;

import com.jira.project.model.entity.TB_JLL_Entity;
import com.jira.project.model.entity.TB_JML_Entity;
import com.jira.project.service.JiraProject;
import com.jira.project.model.dto.CreateBulkResultDTO;
import com.jira.project.model.dto.ProjcetCodeDTO;
import com.jira.project.model.dto.ProjectDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*
 *  해당 컨트롤러는 디비에 있는데이터를 지라 서버로 이관하기 위한 컨트롤러이다
 * */
@RestController
@RequestMapping("/jira/project")
public class JiraProjectController {

    @Autowired
    private JiraProject jiraProject;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
        value={"/search"},
            method={RequestMethod.GET}
    )
    public List<TB_JML_Entity> getJiraProjectListBySearchKeywordOnJML(@RequestParam String searchKeyword) throws Exception{

        logger.info("[::TransferProjectController::] 지라에 생성된 프로젝트 목록 키워드로 검색");
        return jiraProject.getJiraProjectListBySearchKeywordOnJML(searchKeyword);

    }


    @ResponseBody
    @RequestMapping(
            value = {"/{personalId}/create/bulk"},
            method = {RequestMethod.POST}
    )
    public CreateBulkResultDTO createBulkProjectFrom(@PathVariable int personalId, @RequestBody ProjcetCodeDTO projectCodeDTO ) throws Exception {

        // ProjcetCodeDTO projectCodeDTO = projectCodeDTO[0];
        logger.info("프로젝트 생성");
        List<String> success = new ArrayList<>();
        List<String> fail = new ArrayList<>();
        List<String> search_fail = new ArrayList<>();
        List<String> already = new ArrayList<>();

        Map<String, String> result = new HashMap<>();

        for(int i=0;i<projectCodeDTO.getProjectCode().size();i++){
            String projectCode = projectCodeDTO.getProjectCode().get(i);
            result = jiraProject.createProjectFromDB(personalId, projectCode);

            // 이관 실패인 경우
            if (result.containsKey("이관 실패") && result.get("이관 실패").equals(projectCode)) {
                fail.add(projectCode);
            }

            // 프로젝트 조회 실패인 경우
            if (result.containsKey("프로젝트 조회 실패") && result.get("프로젝트 조회 실패").equals(projectCode)) {
                search_fail.add(projectCode);
            }

            // 이관 성공인 경우
            if (result.containsKey("이관 성공") && result.get("이관 성공").equals(projectCode)) {
                success.add(projectCode);
            }

            // 이미 이관한 프로젝트인 경우
            if (result.containsKey("이미 이관한 프로젝트") && result.get("이미 이관한 프로젝트").equals(projectCode)) {
                already.add(projectCode);
            }



        }

        CreateBulkResultDTO createBulkResultDTO = new CreateBulkResultDTO();
        createBulkResultDTO.setFail(fail);
        createBulkResultDTO.setSearchFail(search_fail);
        createBulkResultDTO.setSuccess(success);
        createBulkResultDTO.setAllready(already);

        return createBulkResultDTO;
    }

    /*
     *  생성된 프로젝트의 담당자를 변경하는 컨틀롤러
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/assignee"},
            method = {RequestMethod.PUT}
    )
    public ProjectDTO reassignProjectLeader(@RequestParam String jiraProjectCode, @RequestParam String assignee) throws Exception {
        logger.info("[::TransferProjectController::] 프로젝트 담당자 지정 변경 컨트롤러");
        return  jiraProject.reassignProjectLeader(jiraProjectCode,assignee );
    }

    /*
     *  지라키로 프로젝트 조회하는 API
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/jira"},
            method = {RequestMethod.GET}
    )
    public ProjectDTO getJiraProjectInfoByJiraKey(@RequestParam String jiraKey) throws Exception {
        logger.info("[::TransferProjectController::] 지라키로 프로젝트 조회 컨틀롤러");
        return  jiraProject.getJiraProjectInfoByJiraKey(jiraKey);
    }

    /*
     *  지라 프로젝트 삭제 API
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/delete"},
            method = {RequestMethod.DELETE}
    )
    public List<Map<String, String>> deleteJiraProject(@RequestBody List<String> jiraProjectCodes) throws Exception {
        logger.info("[::TransferProjectController::] 지라 프로젝트 삭제");
        return jiraProject.deleteJiraProject(jiraProjectCodes);
    }

    /*
     *  프로젝트 연결 관계 디비에 저장하는 API (DB 기준)
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/save/relation"},
            method = {RequestMethod.POST}
    )
    public List<TB_JLL_Entity> saveProjectsRelation() throws Exception {
        logger.info("[::TransferProjectController::] 프로젝트 연결관계 디비에 저장하는 API");
        return  jiraProject.saveProjectsRelation();
    }
}