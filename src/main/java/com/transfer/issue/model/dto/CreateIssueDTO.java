package com.transfer.issue.model.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateIssueDTO {
    String jiraKey;
    String projectCode;
}
