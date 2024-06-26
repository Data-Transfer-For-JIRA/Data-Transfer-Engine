package com.jira.account.model.entity;

import lombok.*;
import javax.persistence.*;

@Getter
@Setter
@Builder
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_JIRA_USER",schema="dbo")
public class TB_JIRA_USER_Entity {

    @Column(name = "JU_NAME")
    private String displayName;
    @Id
    @Column(name = "JU_ID")
    private String accountId;

    @Column(name = "JU_EMAIL")
    private String emailAddress;

    @Column(name = "JU_TEAM")
    private String team;

    @Column(name = "JU_PART")
    private String part;


}
