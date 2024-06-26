package com.jira.project.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignProjectDTO {
    String jiraProjectCode;
    String assignee;
    String assigneeId;
}
