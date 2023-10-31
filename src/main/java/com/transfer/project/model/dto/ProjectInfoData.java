package com.transfer.project.model.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInfoData {

    private String self;
    private Integer id;
    private String key;
}
