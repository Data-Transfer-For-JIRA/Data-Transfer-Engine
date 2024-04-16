package com.jira.project.model.entity;


import lombok.*;


import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_JML",schema="dbo")
public class TB_JML_Entity {

    @Id
    @Column(name = "JP_KEY")
    private String key;

    @Column(name = "JP_ID")
    private String id;

    @Column(name = "JP_NAME")
    private String jiraProjectName;

    @Column(name = "M_DATE", nullable = false, updatable = false)
    private LocalDateTime  migratedDate;

    @Column(name = "WP_CODE")
    private String projectCode;

    @Column(name = "WP_NAME")
    private String wssProjectName;

    @Column(name = "FLAG")
    private String flag;

    @Column(name = "WP_ASSIGNEES")
    private String projectAssignees;

    @Column(name = "U_DATE")
    private LocalDateTime updateDate;

    @Column(name = "JI_U_FLAG")
    private Boolean updateIssueFlag;

    @Column(name = "JP_LEADER")
    private String jiraProjectLeader;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.migratedDate = now;
        this.updateIssueFlag = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updateDate = LocalDateTime.now();
    }

}
