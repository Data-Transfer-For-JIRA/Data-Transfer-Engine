package com.jira.account.model.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    private String id;
    private String pwd;
}
