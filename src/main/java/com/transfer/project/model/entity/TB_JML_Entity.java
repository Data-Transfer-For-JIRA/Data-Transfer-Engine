package com.transfer.project.model.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;


import javax.persistence.*;
import java.util.Date;

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

    @Column(name = "JP_NAME")
    private String jiraProjectName;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "M_DATE")
    private Date migratedDate;

    @Column(name = "WP_CODE")
    private String projectCode;

    @Column(name = "WP_NAME")
    private String wssProjectName;


}
