package com.transfer.project.controller;

import com.transfer.project.model.ProjectInfo;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


/*
 *  해당 컨트롤러는 디비에 있는데이터를 지라 서버로 이관하기 위한 컨트롤러이다
 * */
@RestController
@RequestMapping("/transfer/projcet")
public class TransferDataController {

    /*
    * 프로젝트 생성 하기위한 컨트롤러
    * */
    @ResponseBody
    @RequestMapping(
            value = {"/create"},
            method = {RequestMethod.POST}
    )
    public int TransferProjectData(@RequestBody ProjectInfo projectInfo,
                               ModelMap model, HttpServletRequest request) throws Exception {
        return 0;
    }

    /*
     *
     * */
}
