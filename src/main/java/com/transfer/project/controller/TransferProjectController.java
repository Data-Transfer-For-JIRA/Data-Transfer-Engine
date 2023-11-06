package com.transfer.project.controller;

import com.transfer.project.model.dto.ProjectCreateDTO;
import com.transfer.project.model.dto.ProjectInfoData;
import com.transfer.project.model.entity.TB_PJT_BASE_Entity;
import com.transfer.project.service.TransferProjcet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    public List<TB_PJT_BASE_Entity> GetDataBeforeSearchProjectData(@RequestParam String seachKeyWord) throws Exception {

        logger.info("이관전 목록에서 검색");

        return transferProjcet.getDataBeforeSeachProjectData(seachKeyWord);
    }


    /*
     * DB에서 이관 된 프로젝트 정보 가져오는 컨트롤러 박민흠짱
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/after/list"},
            method = {RequestMethod.GET}
    )
    public Page<TB_PJT_BASE_Entity> GetDataAfterProjectData(@RequestParam int pageIndex, @RequestParam int pageSize) throws Exception {

        logger.info("디비 목록 조회");

        return transferProjcet.getDataAfterProjectData(pageIndex,pageSize);
    }

    /*
     * DB에서 이관 된 프로젝트 정보 가져오는 컨트롤러 박민흠짱
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/after/list/search"},
            method = {RequestMethod.GET}
    )
    public List<TB_PJT_BASE_Entity> GetDataAfterSearchProjectData(@RequestParam String seachKeyWord) throws Exception {

        logger.info("이관후 목록에서 검색");

        return transferProjcet.getDataAfterSeachProjectData(seachKeyWord);
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
    public Map<String, Boolean> CreateProjectFrom(@PathVariable int personalId, @RequestParam String projectCode ) throws Exception {

        logger.info("프로젝트 생성");

        return transferProjcet.CreateProjectFromDB(personalId,projectCode);
    }

}
