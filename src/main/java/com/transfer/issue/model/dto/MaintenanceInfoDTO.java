package com.transfer.issue.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintenanceInfoDTO extends CustomFieldDTO {

    // 유지보수 기본 정보 필드
    @JsonProperty("maintenance_name")
    private String customfield_10420;

    @JsonProperty("maintenance_code")
    private String customfield_10421;

    @JsonProperty("duedate")
    private String duedate;

    @JsonProperty("job_title")
    private String customfield_10138;

    @JsonProperty("contract_status")
    private Field customfield_10274;

    @JsonProperty("start_date")
    private String customfield_10134;

    @JsonProperty("verification_url")
    private String customfield_10183;

    @JsonProperty("maintenance_start_date")
    private String customfield_10412;

    @JsonProperty("maintenance_end_date")
    private String customfield_10413;

    @JsonProperty("inspection_method")
    private Field customfield_10417;

    @JsonProperty("inspection_cycle")
    private Field customfield_10419;

    @JsonProperty("inspection_method_etc")
    private String customfield_10418;

}
