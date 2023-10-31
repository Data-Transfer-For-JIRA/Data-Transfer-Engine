package com.transfer.project.model.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Getter
@Setter
@Builder
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "TB_JML",schema="dbo")
public class TB_JML_Entity {

    @Id
    private String key;

    private String jiraProjectName;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date migratedDate;

    private String projectCode;

    private String wssProjectName;


}
