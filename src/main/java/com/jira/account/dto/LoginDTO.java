package com.jira.account.dto;

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
