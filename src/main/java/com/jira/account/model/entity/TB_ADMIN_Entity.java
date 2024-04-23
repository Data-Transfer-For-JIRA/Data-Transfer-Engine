package com.jira.account.model.entity;


import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Builder
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_ADMIN",schema="dbo")
public class TB_ADMIN_Entity {

    @Id
    @Column(name = "P_ID")
    private Integer personalId;

    @Column(name = "J_ID")
    private String id;

    @Column(name = "J_TOKEN")
    private String token;

    @Column(name = "J_URL")
    private String url;

}
