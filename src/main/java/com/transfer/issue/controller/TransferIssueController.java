package com.transfer.issue.controller;

import com.transfer.issue.model.dto.CreateIssueDTO;
import com.transfer.issue.service.TransferIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/transfer/issue")
public class TransferIssueController {

    @Autowired
    private TransferIssue transferIssue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /*
     * 해당 프로젝트에 이슈 생성
     * */
    @ResponseBody
    @RequestMapping(
            value = {""},
            method = {RequestMethod.POST}
    )
    public Map<String ,String> TransferIssueData(@RequestBody String projectCode ) throws Exception {
        logger.info("이슈 생성 컨트롤러 진입");
        return transferIssue.transferIssueData(projectCode);
    }

}


