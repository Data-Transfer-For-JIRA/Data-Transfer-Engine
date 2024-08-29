package com.jira.issue.model.entity.backup;

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
@Table(name = "BACKUP_ISSUE",schema="dbo")
public class BACKUP_ISSUE_Entity {

    @Id
    @Column(name = "JIRA_ISSUE_KEY")
    private String 지라_이슈_키;

    @Column(name = "JIRA_PROJECT_KEY")
    private String 지라_프로젝트_키;

    @Column(name = "JIRA_ISSUE_DESCRIPTION")
    private String 상세내용;

    @Column(name = "JIRA_ISSUE_TITLE")
    private String 지라_이슈_제목;

    @Column(name = "ASSIGNEE")
    private String 담당자;

    @Column(name = "CREATE_DATE")
    private Date 생성일;

    @Column(name = "UPDATE_DATE")
    private Date 업데이트일;

    @Column(name = "DATA_FROM")
    private Boolean 이슈_출처; // true: 지라 ,false: wss
}
