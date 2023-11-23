package com.transfer.issue.model.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Builder
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PJ_PG_SUB",schema="dbo")
public class PJ_PG_SUB_Entity {

    @Column(name = "PJT_ID")
    private String projectId;

    @Column(name = "SUB_CD")
    private String projectCode;

    @Temporal(TemporalType.DATE)
    @Column(name = "SUB_DT")
    private String creationDate;

    @Column(name = "SUB_PS")
    private String writer;

    @Column(name = "SUB_CO")
    private String issueContent;
}
