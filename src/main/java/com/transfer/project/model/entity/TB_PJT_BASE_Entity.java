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
    //이관 여부 플레그

    //============================================== 왼쪽 데이터 ============================
    @Column(name = "BS_CLIENT")
    private String 고객사;
    //고객사
    @Column(name = "BS_CONTRACTOR")
    private String 계약사;
    //계약사
    @Column(name = "BS_LINK")
    private String 연동;
    //연동
    @Column(name = "BS_BUSINESS")
    private String 영업담당자;

    @Column(name = "BS_CONYN")
    private Boolean 계약여부;

    @Column(name = "BS_CHKTYPE")
    private String 점검방식;

    @Column(name = "BS_STEP")
    private int 프로젝트단계;

    // 연락처

    // 관련 프로젝트

    // 제품

    // 연동

    // 바코드

    // 지원 범위

    // 지원 OS

    // 찌원 BS

    // URL

    // 데이터 파일

    // 모듈 버전


}
