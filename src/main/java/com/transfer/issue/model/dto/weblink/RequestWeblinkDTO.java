package com.transfer.issue.model.dto.weblink;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestWeblinkDTO {
    String issueIdOrKey;
    String jiraKey;
    String title;
}
