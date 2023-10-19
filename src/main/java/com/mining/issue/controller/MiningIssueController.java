package com.mining.issue.controller;


import com.mining.issue.service.MiningIssue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/*
*  해당 컨트롤러는 지라서버에서 새로 추가된 이슈를 확인하여 해당 이슈 정보를 DB에 적제 하는 컨트롤러이다.
* */
@RestController
@RequestMapping("/mining")
public class MiningIssueController {


    @Autowired
    @Qualifier("MiningIssue")
    private MiningIssue miningIssue;

    /*
    *  어떤 프로젝트에 어떤 이슈가 새로 생성되었는지 Return 해주는 컨트롤러 프로젝트 키 이슈 키 형태로 리턴 해줌
    * */
    @ResponseBody
    @RequestMapping(
            value = {"/scheduler"},
            method = {RequestMethod.GET}
    )
    public List<Map<String,String>> miningscheduler(ModelMap model, HttpServletRequest request) throws Exception {

        return null;
    }

    /*
    *  새로 생성된 프로젝트와 이슈를 조회하여 DB에 적제하는 컨트롤러
    * */
    @ResponseBody
    @RequestMapping(
            value = {"/issue"},
            method = {RequestMethod.GET}
    )
    public int miningissuedata(@RequestParam("project_key") String project_key,
                               @RequestParam("issue_key") String issue_key,
                                     ModelMap model, HttpServletRequest request) throws Exception {
        return 0;
    }
}
