package com.api.scheduler.backup.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jira.project.model.entity.TB_JML_Entity;
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
@Table(name = "BACKUP_BASEINFO_P",schema="dbo")
public class BACKUP_BASEINFO_P_Entity {
    @Id
    @Column(name = "JIRA_PROJECT_KEY")
    private String 지라_프로젝트_키;

    @Column(name = "PROJECT_NAME")
    private String 프로젝트_명;

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

    @Column(name = "PROJECT_ASSIGNMENT_DATE")
    private Date 프로젝트_배정일;

    @Column(name = "PROJECT_PROGRESS_STEP")
    private String 프로젝트_진행_단계;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JIRA_PROJECT_KEY", insertable = false, updatable = false)
    @JsonBackReference
    private TB_JML_Entity 프로젝트_기본정보;
}
