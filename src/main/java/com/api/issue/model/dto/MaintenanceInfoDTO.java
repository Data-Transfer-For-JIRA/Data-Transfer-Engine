package com.api.issue.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintenanceInfoDTO extends CustomFieldDTO {

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
