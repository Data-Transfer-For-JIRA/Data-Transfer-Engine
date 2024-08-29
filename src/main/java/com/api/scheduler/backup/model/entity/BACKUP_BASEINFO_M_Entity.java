package com.api.scheduler.backup.model.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Builder
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BACKUP_BASEINFO_M",schema="dbo")
public class BACKUP_BASEINFO_M_Entity {
    @Id
    @Column(name = "JIRA_PROJECT_KEY")
    private String 지라_프로젝트_키;

    @Column(name = "MAINTENANCE_NAME")
    private String 유지보수_명;

    @Column(name = "MAIN_ASSIGNEE")
    private String 담당자_정;

    @Column(name = "SUB_ASSIGNEE")
    private String 담당자_부;

    @Column(name = "SALES_MANAGER")
    private String 영업_대표;

    @Column(name = "CONTRACTOR")
    private String 계약사;

    @Column(name = "CLIENT")
    private String 고객사;

    @Column(name = "BARCODE_TYPE")
    private String 바코드_타입;

    @Column(name = "MULTI_OS_SUPPORT")
    private String 멀티_OS_지원여부;

    @Column(name = "RELATE_PROJECT_KEY")
    private String 연관된_프로젝트_키;


    @Column(name = "PRODUCT_INFO1")
    private String 제품_정보1;

    @Column(name = "PRODUCT_INFO2")
    private String 제품_정보2;

    @Column(name = "PRODUCT_INFO3")
    private String 제품_정보3;

    @Column(name = "PRODUCT_INFO4")
    private String 제품_정보4;

    @Column(name = "PRODUCT_INFO5")
    private String 제품_정보5;

    ///////////////////////공통//////////////////////////////

    @Column(name = "CONTRACT_STATUS")
    private String 계약_여부;

    @Column(name = "START_DATE")
    private Date 유지보수_시작일;

    @Column(name = "END_DATE")
    private Date 유지보수_종료일;

    @Column(name = "INSPECTION_METHOD")
    private String 점검_방법;

    @Column(name = "INSPECTION_CYCLE")
    private String 점검_주기;

    @Column(name = "PRINTER_SUPPORT_RANGE")
    private String 프린터_지원_범위;


    ////////////////////////////////////////////////////////

    @Column(name = "UPDATE_DATE")
    private LocalDateTime 업데이트_시간;

    @PrePersist // 처음 저장 
    @PreUpdate // 업데이트
    public void preUpdate() {
        this.업데이트_시간 = LocalDateTime.now();
    }

}
