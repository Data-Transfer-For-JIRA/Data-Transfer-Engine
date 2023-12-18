package com.transfer.issue.model.dto;

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
    @JsonProperty("customfield_10420")
    private String maintenanceName;

    @JsonProperty("customfield_10421")
    private String maintenanceCode;

    @JsonProperty("customfield_10274")
    private Field contractStatus;

    @JsonProperty("customfield_10412")
    private String maintenanceStartDate;

    @JsonProperty("customfield_10413")
    private String maintenanceEndDate;

    @JsonProperty("customfield_10417")
    private Field inspectionMethod;

    @JsonProperty("customfield_10419")
    private Field inspectionCycle;

    @JsonProperty("customfield_10418")
    private String inspectionMethodEtc;

    @JsonProperty("customfield_10183")
    private String verificationUrl;

    private String duedate;

}
