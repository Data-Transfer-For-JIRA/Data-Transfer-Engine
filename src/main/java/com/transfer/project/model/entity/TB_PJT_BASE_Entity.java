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
    // 프로젝트 이름

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
    // 고객사
    @Column(name = "BS_CLIENT")
    private String 고객사;

    // 계약사
    @Column(name = "BS_CONTRACTOR")
    private String 계약사;
    
    // 영업 담당자
    @Column(name = "BS_BUSINESS")
    private String 영업담당자;

    // 계약 여부
    @Column(name = "BS_CONYN")
    private Boolean 계약여부;

    // 점검 방식
    @Column(name = "BS_CHKTYPE")
    private String 점검방식;

    @Column(name = "BS_STEP")
    private int 프로젝트단계;
    /*
    * 0: 사전프로젝트
    * 1: 확정프로젝트
    * 5: 무상유지보수
    * 7: 유상유지보수
    * 8: 개발과제
    * 9: 기타
    * 91:유지보수 종료
    * 92:프로젝트 종료(M 필요없음)
    * 93:프로젝트 종료(M 계약)
    * 94:프로젝트 종료(M 미계약)
    * */

    // 연락처 => WSS에 데이터가 정형화되어 들어가있지 않음
    // 고객사 담당자 이름
    @Column(name = "BS_SITESTAFF")
    private String 담당자이름;

    // 고객사 담당자 연락처
    @Column(name = "BS_SITEPHONE")
    private String 담당자연락처;

    // 고객사 담당자 이메일
    @Column(name = "BS_SITEMAIL")
    private String 담당자이메일;

    // 관련 프로젝트
    @Column(name = "BS_PARENTS_PJTCD")
    private String BS_PARENTS_PJTCD;
    /*
    *  관련 프로젝트 필드에는 해당 프로젝트의 유지보수 코드가 연결되어있음
    * */

    // 제품
    @Column(name = "BS_PRODUCTTYPE")
    private int 제품;
    /*
    *  제품 타입
    *  zero 12
    *  exe  11
    *  activx 10
    * */

    @Column(name = "BS_LINK")
    private String 연동;
    /*
     * 연동
     * HTML, OZ,입력 데이터
     * */

    // 바코드

    // 지원 범위

    // 지원 OS

    // 찌원 BS

    // URL

    // 데이터 파일

    // 모듈 버전


}
