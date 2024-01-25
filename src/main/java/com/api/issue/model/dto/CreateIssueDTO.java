package com.api.issue.model.dto;

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