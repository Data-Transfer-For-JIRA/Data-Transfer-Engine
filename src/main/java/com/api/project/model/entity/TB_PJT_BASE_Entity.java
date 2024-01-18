package com.api.project.model.entity;


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
    //프로젝트 이관 여부 플레그

    @Column(name = "MI_FLAG")
    private Boolean issueMigrateFlag;
    //이슈 이관 여부 플레그

    //============================================== 왼쪽 데이터 ============================
    // 고객사
    @Column(name = "BS_CLIENT")
    private String client;

    // 계약사
    @Column(name = "BS_CONTRACTOR")
    private String contractor;
    
    // 영업 대표
    @Column(name = "BS_BUSINESS")
    private String salesManager;

    // 담당 엔지니어
    @Column(name = "BS_MASTAFF")
    private String assignedEngineer;


    // 계약 여부
    @Column(name = "BS_CONYN")
    private String contract;

    // 점검 방식
    @Column(name = "BS_CHKTYPE")
    private String inspectionType;

    // 계약 기간 시작
    @Column(name = "BS_REALSTARTDATE")
    private Date contractStartDate;

    // 계약 기간 종료
    @Column(name = "BS_REALENDDATE")
    private Date contractEndDate;

    // 프로젝트단계
    @Column(name = "BS_STEP")
    private int projectStep;
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
    
    /*======================연락처========================*/
    // 연락처 => WSS에 데이터가 정형화되어 들어가있지 않음
    // 고객사 담당자 이름
    @Column(name = "BS_SITESTAFF")
    private String clientName;

    // 고객사 담당자 연락처
    @Column(name = "BS_SITEPHONE")
    private String clientPhoneNumber;

    // 고객사 담당자 이메일
    @Column(name = "BS_SITEMAIL")
    private String clientEmail;
    /*=================================================*/
    
    // 관련 프로젝트
    @Column(name = "BS_PARENTS_PJTCD")
    private String relatedProject;
    /*
    *  관련 프로젝트 필드에는 해당 프로젝트의 유지보수 코드가 연결되어있음
    * */

    // 제품
    @Column(name = "BS_PRODUCTTYPE")
    private int productType;
    /*
    *  제품 타입
    *  zero 12
    *  exe  11
    *  activx 10
    * */

    // 연동 방법
    @Column(name = "BS_LINK")
    private String connectionType;
    /*
     * 연동
     * HTML, OZ,입력 데이터
     * */
    // WebDRM인 경우  BS_PRODUCTTYPE는 exe 이면서 연동은 WebDRM이다

    // 바코드 BS_BARCODETYPE
    @Column(name = "BS_BARCODETYPE")
    private int barcodeType;
    /*
    *  기본 : 0 , 3단 : 1
    * */

    // 지원 범위 BS_PRINTER
    @Column(name = "BS_PRINTER")
    private String printer;
    /*
    *  모두 지원/공유 지원
    *  모두 지원/공유 불가
    *  등록 지원/공유 지원
    *  등록 지원/공유 불가
    * */

    // 지원 OS BS_OS
    @Column(name = "BS_OS")
    private String supportType;
    /*
    *  Windows
    *  Multi OS
    * */


    // 모듈 버전 BS_CABVER
    @Column(name = "BS_CABVER")
    private String clientType;

    // URL
    @Column(name = "BS_URL")
    private String url;
}
