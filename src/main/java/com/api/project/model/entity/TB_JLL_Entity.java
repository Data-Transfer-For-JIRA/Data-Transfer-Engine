package com.api.project.model.entity;

import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Builder
@Table(name = "TB_JLL",schema="dbo")
public class TB_JLL_Entity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "P_JIRAKEY")
    private String parentKey;

    @Column(name = "C_JIRAKEY")
    private String childKey;


    @Column(name = "L_FLAG")
    private Boolean linkCheckFlag;
}
