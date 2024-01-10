package com.transfer.project.controller;

import com.transfer.project.model.dto.*;
import com.transfer.project.model.entity.TB_JLL_Entity;
import com.transfer.project.model.entity.TB_JML_Entity;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;
import com.transfer.project.service.TransferProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*
 *  해당 컨트롤러는 디비에 있는데이터를 지라 서버로 이관하기 위한 컨트롤러이다
 * */
@RestController
@RequestMapping("/transfer/project")
public class TransferProjectController {

    @Autowired
    private TransferProject transferProject;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*
    * 지라 프로젝트 생성 하기위한 컨트롤러
    * */
    @ResponseBody
    @RequestMapping(
            value = {"/create/test"},
            method = {RequestMethod.POST}
    )
    public CreateProjectResponseDTO CreateProjectData(@RequestBody CreateProjectDTO createProjectDTO,
                                                      ModelMap model, HttpServletRequest request) throws Exception {

        return transferProject.createProject(createProjectDTO);
    }
    /*
     * DB에서 프로젝트 정보 가져오는 컨트롤러
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/db/list"},
            method = {RequestMethod.GET}
    )
    public Page<TB_PJT_BASE_Entity> GetDataBaseProjectData(@RequestParam int pageIndex, @RequestParam int pageSize) throws Exception {

        logger.info("디비 목록 조회");

        return transferProject.getDataBaseProjectData(pageIndex,pageSize);
    }

    /*
     * DB에서 이관 전 프로젝트 정보 가져오는 컨트롤러
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/before/list"},
            method = {RequestMethod.GET}
    )
    public Page<TB_PJT_BASE_Entity> GetDataBeforeProjectData(@RequestParam int pageIndex, @RequestParam int pageSize) throws Exception {

        logger.info("디비 목록 조회");

        return transferProject.getDataBeforeProjectData(pageIndex,pageSize);
    }
    /*
     * DB에서 이관 전 프로젝트 정보 프로젝트 이름으로 가져오는 컨트롤러
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/before/list/search"},
            method = {RequestMethod.GET}
    )
    public Page<TB_PJT_BASE_Entity> GetDataBeforeSearchProjectData(@RequestParam String searchKeyWord, @RequestParam int pageIndex, @RequestParam int pageSize) throws Exception {

        logger.info("이관전 목록에서 검색");
      /*  if(searchKeyWord.isEmpty()){
            transferProjcet.getDataBeforeProjectData(pageIndex,pageSize);
        }*/

        return transferProject.getDataBeforeSeachProjectData(searchKeyWord,pageIndex,pageSize);
    }


    /*
     * DB에서 이관 된 프로젝트 정보 가져오는 컨트롤러
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/after/list"},
            method = {RequestMethod.GET}
    )
    public Page<TB_JML_Entity>  GetDataAfterProjectData(@RequestParam int pageIndex, @RequestParam int pageSize) throws Exception {

        logger.info("디비 목록 조회");

        return transferProject.getDataAfterProjectData(pageIndex,pageSize);
    }

    /*
     * DB에서 이관 된 프로젝트 정보 가져오는 컨트롤러
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/after/list/search"},
            method = {RequestMethod.GET}
    )
    public Page<TB_JML_Entity>  GetDataAfterSearchProjectData(@RequestParam String searchKeyWord, @RequestParam int pageIndex, @RequestParam int pageSize) throws Exception {

        logger.info("이관후 목록에서 검색");

        return transferProject.getDataAfterSearchProjectData(searchKeyWord,pageIndex,pageSize);
    }
    /*
     *  해당 지라키가 지라 서버에 존재하는지 확인하는 컨트롤러
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/all"},
            method = {RequestMethod.GET}
    )
    public Page<TB_PJT_BASE_Entity>  getTransferredProjectsList(@RequestParam int pageIndex, @RequestParam int pageSize) throws Exception {
        return  transferProject.getTransferredProjectsList(pageIndex,pageSize);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{personalId}/create"},
            method = {RequestMethod.POST}
    )
    public Map<String, String> CreateProjectFrom(@PathVariable int personalId, @RequestParam String projectCode ) throws Exception {

        logger.info("프로젝트 생성");

        return transferProject.CreateProjectFromDB(personalId,projectCode);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{personalId}/create/bulk"},
            method = {RequestMethod.POST}
    )
    //public CreateBulkResultDTO CreateBulkProjectFrom(@PathVariable int personalId, @RequestBody ProjcetCodeDTO[] projectCodeDTO ) throws Exception {
    public CreateBulkResultDTO CreateBulkProjectFrom(@PathVariable int personalId, @RequestBody ProjcetCodeDTO projectCodeDTO ) throws Exception {

        // ProjcetCodeDTO projectCodeDTO = projectCodeDTO[0];
        logger.info("프로젝트 생성");
        List<String> success = new ArrayList<>();
        List<String> fail = new ArrayList<>();
        List<String> search_fail = new ArrayList<>();
        List<String> already = new ArrayList<>();

        Map<String, String> result = new HashMap<>();

        for(int i=0;i<projectCodeDTO.getProjectCode().size();i++){
            String projectCode = projectCodeDTO.getProjectCode().get(i);
            result = transferProject.CreateProjectFromDB(personalId, projectCode);

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
    *  해당 지라키가 지라 서버에 존재하는지 확인하는 컨트롤러
    * */
    @ResponseBody
    @RequestMapping(
            value = {"/check/jirakey"},
            method = {RequestMethod.GET}
    )
    public Boolean  checkJiraKey(@RequestParam String jiraKey) throws Exception {
        return  transferProject.checkValidationJiraKey(jiraKey);
    }
    /*
     *  생성된 프로젝트의 담당자를 변경하는 컨틀롤러
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/assignee"},
            method = {RequestMethod.PUT}
    )
    public ProjectDTO reassignProjectLeader(@RequestParam String jiraProjectCode,@RequestParam String assignee) throws Exception {
        logger.info("[::TransferProjectController::] 프로젝트 담당자 지정 변경 컨트롤러");
        return  transferProject.reassignProjectLeader(jiraProjectCode,assignee );
    }

    /*
     *  지라키로 프로젝트 조회하는 API
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/jira"},
            method = {RequestMethod.GET}
    )
    public ProjectDTO  getJiraProjectInfoByJiraKey(@RequestParam String jiraKey) throws Exception {
        logger.info("[::TransferProjectController::] 지라키로 프로젝트 조회 컨틀롤러");
        return  transferProject.getJiraProjectInfoByJiraKey(jiraKey);
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
        return transferProject.deleteJiraProject(jiraProjectCodes);
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
        return  transferProject.saveProjectsRelation();
    }
}