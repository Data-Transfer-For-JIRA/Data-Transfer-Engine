package com.jira.issue.model.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jira.issue.model.dto.FieldDTO.Description;
import com.jira.issue.model.dto.FieldDTO.Field;
import com.jira.issue.model.dto.FieldDTO.Project;
import com.jira.issue.model.dto.FieldDTO.User;
import com.jira.issue.model.dto.search.SearchCustomFieldDTO.Team;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchMaintenanceInfoDTO {

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

    // 유지보수 기본 정보 필드
    // 유지보수 명
    @JsonProperty("customfield_10420")
    private String maintenanceName;
    // 유지보수 코드
    @JsonProperty("customfield_10421")
    private String maintenanceCode;
    // 계약 여부
    @JsonProperty("customfield_10274")
    private Field contractStatus;
    // 유지보수 시작일
    @JsonProperty("customfield_10412")
    private String maintenanceStartDate;
    // 유지보수 종료일
    @JsonProperty("customfield_10413")
    private String maintenanceEndDate;
    // 점검방법
    @JsonProperty("customfield_10417")
    private Field inspectionMethod;
    // 점검주기
    @JsonProperty("customfield_10419")
    private Field inspectionCycle;
    // 점검방법 기타
    @JsonProperty("customfield_10418")
    private String inspectionMethodEtc;
    // 검증 url
    @JsonProperty("customfield_10183")
    private String verificationUrl;
    // 기한
    private String duedate;

}