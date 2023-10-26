package com.transfer.project.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Entity
@Builder
@Table(name = "TB_PJT_BASE")
public class TB_PJT_BASE_Entity {

    @Column(unique = true) // primary key
    private String BS_PJTCD;
    // 프로젝트 코드
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //unique key
    private Long BS_PJTNAME;


    private String BS_PJFLAG;
    // 프로젝트 P , 유지보수 M

    private Date BS_SYSDATE;
    // 생성시간
}
