package com.transfer.issuetype.controller;

import com.transfer.issuetype.model.dto.IssueTypeConnectDTO;
import com.transfer.issuetype.service.TransferIssueType;
import com.transfer.project.model.dto.ProjectCreateDTO;
import com.transfer.project.model.dto.ProjectInfoData;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/*
 *  해당 컨트롤러는 이관한 지라 프로젝트에 이슈 타입을 연결하기 위한 컨트롤러이다.
 * */
@RestController
@RequestMapping("/transfer/issuetype")
public class TransferIssueTypeController {

    @Autowired
    private TransferIssueType transferIssueType;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());



    /*
     * 지라 이슈 타입 연결하기 위한 컨트롤러
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/set"},
            method = {RequestMethod.PUT}
    )
    public void SetIssueType(@RequestParam String projectId) throws Exception {

        transferIssueType.setIssueType(projectId);

    }

    /*
     * 생성한 모든 프로젝트에 이슈 타입 연결하기 위한 컨트롤러
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/setall"},
            method = {RequestMethod.PUT}
    )
    public void SetIssueTypeScheduler() throws Exception {
        transferIssueType.setIssueTypeScheduler();

    }

}
