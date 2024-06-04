package com.jira.issue.model.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jira.issue.model.dto.FieldDTO.Description;
import com.jira.issue.model.dto.FieldDTO.Field;
import com.jira.issue.model.dto.FieldDTO.Project;
import com.jira.issue.model.dto.FieldDTO.User;
import com.jira.issue.model.dto.search.SearchCustomFieldDTO.Team;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchProjectInfoDTO {

    // 기본 필드
    private Project project;

    private Field issuetype;

    private Field status;

    private String summary;

    private Description description;

    private User assignee;

    // 공통 커스텀 필드
    @JsonProperty("customfield_10275")
    private User salesManager; // 영업 담당자

    @JsonProperty("customfield_10270")
    private String contractor; // 계약사

    @JsonProperty("customfield_10271")
    private String client; // 고객사

    @JsonProperty("customfield_10272")
    private Field barcodeType; // 바코드 타입

    @JsonProperty("customfield_10001")
    private Team team; // 팀

    @JsonProperty("customfield_10279")
    private Field part; // 파트

    @JsonProperty("customfield_10269")
    private User subAssignee; // 부 담당자

    @JsonProperty("customfield_10415")
    private List<Field> multiOsSupport; // 멀티 OS 정보

    @JsonProperty("customfield_10247")
    private Field printerSupportRange; // 프린터 지원 여부

    // 제품 정보
    @JsonProperty("customfield_10445")
    private List<Field> productInfo1;

    @JsonProperty("customfield_10446")
    private List<Field> productInfo2;

    @JsonProperty("customfield_10447")
    private List<Field> productInfo3;

    @JsonProperty("customfield_10448")
    private List<Field> productInfo4;

    @JsonProperty("customfield_10449")
    private List<Field> productInfo5;

    // 프로젝트 기본 정보 필드
    @JsonProperty("customfield_10411")
    private String projectName; // 프로젝트 이름

    @JsonProperty("customfield_10410")
    private String projectCode; // 프로젝트 코드

    @JsonProperty("customfield_10414")
    private String projectAssignmentDate; // 프로젝트 할당 일자

    @JsonProperty("customfield_10280")
    private Field projectProgressStep; // 프로젝트 진행 단계

}
