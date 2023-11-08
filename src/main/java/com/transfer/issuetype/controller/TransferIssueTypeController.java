package com.transfer.issuetype.controller;

import com.transfer.issuetype.model.dto.IssueTypeScreenSchemeDTO;
import com.transfer.issuetype.service.TransferIssueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * 지라 이슈 타입 연결하기 위한 컨트롤러 나중에 스케줄러를 위해 임시로 생성해둔 것 프로젝트 생성로직에 포함X
     * */
    @ResponseBody
    @RequestMapping(
            value = {"/set"},
            method = {RequestMethod.PUT}
    )
    public void SetIssueType(@RequestBody IssueTypeScreenSchemeDTO issueTypeScreenSchemeDTO,@RequestParam String flag) throws Exception {

        transferIssueType.setIssueType(issueTypeScreenSchemeDTO,flag);

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
