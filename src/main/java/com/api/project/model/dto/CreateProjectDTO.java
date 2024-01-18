package com.api.project.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateProjectDTO {

    //=================================== 프로젝트 생성(직접생성) =========================================//

    private String assigneeType;
    // 설정값: UNASSIGNED
    // 설명 : 프로젝트에 이슈 생성시 디폴트롤 담당자 설정 PROJECT_LEAD로 설정시 프로젝트 생성자가 담당자가 됨
    private Integer categoryId;
    // 설정 값: 10005
    // 설명 :프로젝트 카테고리
    private String description;
    // 설정 값: 생략 가능
    // 설명 :프로젝트 설명
    private String key;
    // 설정 값: 가변 값
    // 설명 : 프로젝트 키
    private String leadAccountId;
    // 설정 값: 63e5a629724a5c79c7bce462
    // 설명 : 프로젝트 생성자 필수 값
    private String name;
    // 설정 값: 가변 값
    // 설명 : 프로젝트 이름
    private String projectTypeKey;
    // 설정 값: business
    // 설명 : 프로젝트 타입 현재 비지니스로 생성중
    private String url ;
    // 설정 값: https://markany.atlassian.net
    // 설명 : 생성 경로


    //=================================== 프로젝트 생성(프로젝트 템플릿 설정 공유) =========================================//

    private String existingProjectId; // 연결 대상 템플릿


}
