package com.transfer.project.model.dto;


import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectResponseDTO {
    // 프로젝트 기본 생성
    private String self;
    private Integer id;
    private String key;

    // 템플릿을 통한 프로젝트 생성
    private Integer projectId;
    private String projectKey;
    private String projectName;
}
