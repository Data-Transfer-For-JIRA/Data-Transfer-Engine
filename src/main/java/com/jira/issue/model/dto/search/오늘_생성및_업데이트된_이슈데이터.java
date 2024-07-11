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
public class 오늘_생성및_업데이트된_이슈데이터 {

    Long startAt;

    Long maxResults;

    Long total;

    List<SearchIssueDTO<FieldDTO>> issues;
}
