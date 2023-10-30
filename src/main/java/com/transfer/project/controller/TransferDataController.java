package com.transfer.project.controller;

import com.transfer.project.model.ProjectCreateDTO;
import com.transfer.project.model.ProjectInfoData;
import com.transfer.project.model.TB_PJT_BASE_Entity;
import com.transfer.project.service.TransferProjcet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
public class TransferDataController {

    @Autowired
    private TransferProjcet transferProjcet;



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

        return transferProjcet.getDataBaseProjectData(pageIndex,pageSize);
    }


    @ResponseBody
    @RequestMapping(
            value = {"/test"},
            method = {RequestMethod.GET}
    )
    public int  test(@RequestParam int test) throws Exception {
        return  test+10 ;
    }

    /*
     * 해당 프로젝트 정보를 통해 지라 프로젝트 생성
     *
     * 1. 선택한 프로젝트 정보 수신
     * 2. 프로젝트 정보 조회
     * 3. 해당 정보 + 원하는 키 + 원하는 프로젝트 이름
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/create"},
            method = {RequestMethod.POST}
    )
    public Map<String, String> CreateProjectFrom(@RequestParam String projectCode ) throws Exception {

        return transferProjcet.CreateProjectFromDB(projectCode);
    }
}
