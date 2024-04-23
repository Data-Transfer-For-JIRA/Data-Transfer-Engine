package com.jira.issue.model.dto.create;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateIssueDTO<T> {
    private T fields;
}