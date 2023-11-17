package com.transfer.project.controller;

import com.transfer.project.model.dto.CreateBulkResultDTO;
import com.transfer.project.model.dto.ProjcetCodeDTO;
import com.transfer.project.model.dto.ProjectCreateDTO;
import com.transfer.project.model.dto.ProjectInfoData;
import com.transfer.project.model.entity.TB_JML_Entity;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;
import com.transfer.project.service.TransferProjcet;
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
    private TransferProjcet transferProjcet;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*
    * 지라 프로젝트 생성 하기위한 컨트롤러
    * */
    @ResponseBody
    @RequestMapping(
            value = {"/create/test"},
            method = {RequestMethod.POST}
    )
    public ProjectInfoData CreateProjectData(@RequestBody ProjectCreateDTO projectCreateDTO,
                                                    ModelMap model, HttpServletRequest request) throws Exception {

        return transferProjcet.createProject(projectCreateDTO);
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

        return transferProjcet.getDataBaseProjectData(pageIndex,pageSize);
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

        return transferProjcet.getDataBeforeProjectData(pageIndex,pageSize);
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

        return transferProjcet.getDataBeforeSeachProjectData(searchKeyWord,pageIndex,pageSize);
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

        return transferProjcet.getDataAfterProjectData(pageIndex,pageSize);
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

        return transferProjcet.getDataAfterSeachProjectData(searchKeyWord,pageIndex,pageSize);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/test"},
            method = {RequestMethod.GET}
    )
    public int  test(@RequestParam int test) throws Exception {
        return  test+10 ;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{personalId}/create"},
            method = {RequestMethod.POST}
    )
    public Map<String, String> CreateProjectFrom(@PathVariable int personalId, @RequestParam String projectCode ) throws Exception {

        logger.info("프로젝트 생성");

        return transferProjcet.CreateProjectFromDB(personalId,projectCode);
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
        List<String> allready = new ArrayList<>();

        Map<String, String> result = new HashMap<>();

        for(int i=0;i<projectCodeDTO.getProjectCode().size();i++){
            String projectCode = projectCodeDTO.getProjectCode().get(i);
            result = transferProjcet.CreateProjectFromDB(personalId, projectCode);

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
                allready.add(projectCode);
            }



        }

        CreateBulkResultDTO createBulkResultDTO = new CreateBulkResultDTO();
        createBulkResultDTO.setFail(fail);
        createBulkResultDTO.setSearchFail(search_fail);
        createBulkResultDTO.setSuccess(success);
        createBulkResultDTO.setAllready(allready);

        return createBulkResultDTO;
    }

}
