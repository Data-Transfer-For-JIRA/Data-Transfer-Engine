package com.transfer.issue.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WssIssueDTO {
    private String projectCode;
    private String creationDate;
    private String issueContent;
    private String projectFlag;
    private String projectName;
    private String salesManger;
    private String contractor;
    private String client;
    private String productType;
    private String connectionType;
    private String barcodeType;
    private String supportType;
    private String printer;
    private String projectStep;
}
