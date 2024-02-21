package com.jira.issue.model.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Builder
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PJ_PG_SUB",schema="dbo")
@IdClass(PJ_PG_SUB_Entity.ProjectId.class)
public class PJ_PG_SUB_Entity {
    @Id
    @Column(name = "PJT_ID")
    private String projectId;
    @Id
    @Column(name = "SUB_CD")
    private String projectCode;

    @Temporal(TemporalType.DATE)
    @Column(name = "SUB_DT")
    private Date creationDate;

    @Column(name = "SUB_PS")
    private String writer;

    @Column(name = "SUB_CO")
    private String issueContent;

    @Column(name = "MI_FLAG")
    private Boolean issueMigrateFlag;

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectId implements Serializable {
        private String projectId;
        private String projectCode;
    }
}
