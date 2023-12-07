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
public class ProjectInfoDTO extends CustomFieldDTO {

    // 프로젝트 기본 정보 필드
    @JsonProperty("project_name")
    private String customfield_10411;

    @JsonProperty("project_code")
    private String customfield_10410;

    @JsonProperty("project_assignment_date")
    private String customfield_10414;

    @JsonProperty("project_progress_stage")
    private Field customfield_10280;

}
