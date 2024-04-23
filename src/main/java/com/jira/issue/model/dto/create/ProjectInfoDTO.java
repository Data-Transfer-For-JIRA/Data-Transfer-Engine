package com.jira.issue.model.dto.create;

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
public class ProjectInfoDTO extends CustomFieldDTO {

    // 프로젝트 기본 정보 필드
    @JsonProperty("customfield_10411")
    private String projectName;

    @JsonProperty("customfield_10410")
    private String projectCode;

    @JsonProperty("customfield_10414")
    private String projectAssignmentDate;

    @JsonProperty("customfield_10280")
    private Field projectProgressStep;

}
