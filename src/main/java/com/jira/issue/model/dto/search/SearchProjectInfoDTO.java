package com.jira.issue.model.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jira.issue.model.dto.FieldDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchProjectInfoDTO extends SearchCustomFieldDTO{

    @JsonProperty("customfield_10411")
    private String projectName; // 프로젝트 이름

    @JsonProperty("customfield_10410")
    private String projectCode; // 프로젝트 코드

    @JsonProperty("customfield_10414")
    private String projectAssignmentDate; // 프로젝트 할당 일자

    @JsonProperty("customfield_10280")
    private FieldDTO.Field projectProgressStep; // 프로젝트 진행 단계

}
