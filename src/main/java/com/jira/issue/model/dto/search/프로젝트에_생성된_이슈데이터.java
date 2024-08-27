package com.jira.issue.model.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jira.issue.model.dto.FieldDTO;
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
public class 프로젝트에_생성된_이슈데이터 {

    Integer startAt;

    Integer maxResult;

    Integer total;

    List<SearchRenderedIssue> issues;
}

