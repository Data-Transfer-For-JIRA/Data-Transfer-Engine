package com.transfer.project.model.entity;


import lombok.*;

import javax.persistence.*;
import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Builder
@Table(name = "TB_PJT_BASE",schema="dbo")
public class TB_PJT_BASE_Entity {
    @Id
    @Column(name = "BS_PJTCD") // primary key
    private String projectCode;
    // 프로젝트 코드

    @GeneratedValue(strategy = GenerationType.IDENTITY) //unique key
    @Column(unique = true , name = "BS_PJTNAME")
    private String projectName;

    @Column(name = "BS_PJTFLAG")
    private String projectFlag;
    // 프로젝트 P , 유지보수 M

    @Temporal(TemporalType.DATE)
    @Column(name = "BS_SYSDATE")
    private Date createdDate;
    // 생성시간
    @Column(name = "MJ_FLAG")
    private Boolean migrateFlag;

}
