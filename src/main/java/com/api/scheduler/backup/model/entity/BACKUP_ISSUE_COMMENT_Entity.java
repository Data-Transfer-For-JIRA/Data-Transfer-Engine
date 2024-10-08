package com.api.scheduler.backup.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "BACKUP_ISSUE_COMMENT",schema="dbo")
public class BACKUP_ISSUE_COMMENT_Entity {

    @Id
    @Column(name = "COMMENT_ID")
    private String 댓글_아이디;

    @Column(name = "COMMENT")
    private String 댓글_내용;

    @Column(name = "CREATED")
    private Date 생성일;

    @Column(name = "UPDATED")
    private Date 업데이트일;

    @Column(name = "AUTHOR")
    private String 작성자;

    @Column(name = "JIRA_ISSUE")
    private String 지라이슈_키;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JIRA_ISSUE", insertable = false, updatable = false)
    @JsonBackReference
    private BACKUP_ISSUE_Entity 지라이슈;
}
