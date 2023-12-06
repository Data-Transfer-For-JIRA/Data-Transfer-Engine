package com.transfer.issue.model.dto;


import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldDTO {

    // 기본 (필수 필드)
    private Project project;

    private IssueType issuetype;

    private Status status;

    private String summary;

    private Description description;

    // 댓글 필드 추가 필요


    // 커스텀 필드
    private User assignee;

    @JsonProperty("job_title")
    private String customfield_10138;

    @JsonProperty("sub_assignee")
    private User customfield_10269;

    @JsonProperty("sales_manager")
    private User customfield_10275;

    @JsonProperty("contractor")
    private String customfield_10270;

    @JsonProperty("client")
    private String customfield_10271;

    @JsonProperty("start_date")
    private String customfield_10015;

    @JsonProperty("printer_support_scope")
    private Printer customfield_10247;

    @JsonProperty("barcode_type")
    private Barcode customfield_10272;

    @JsonProperty("team")
    private Team customfield_10001;

    @JsonProperty("project_progress_phase")
    private ProjectProgressPhase customfield_10280;

    @JsonProperty("product_type")
    private String customfield_10277;

    @JsonProperty("part")
    private String customfield_10279;

    @JsonProperty("project_name")
    private String customfield_10411;

    @JsonProperty("project_code")
    private String customfield_10410;

    @JsonProperty("project_assignment_date")
    private String customfield_10414;

    @JsonProperty("product_info")
    private String customfield_10406;

    @JsonProperty("product_type_etc")
    private String customfield_10416;

    @JsonProperty("product_info_etc")
    private String customfield_10407;

    @JsonProperty("link_info_etc")
    private String customfield_10409;

    @JsonProperty("multi_os_support")
    private String customfield_10415;


    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Project{
        //String id;
        String key;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class IssueType{
        // id: 10441 (전자문서 - 프로젝트 기본정보)
        // id: 10442 (전자문서 - 유지보수 기본정보)
        // id: 10002 (작업)
        String id;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Status{
        // id: 10000 (해야 할 일)
        // id: 10001 (완료됨)
        String id;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class User{
        String accountId;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Description{
        private List<Content> content;
        private String type;
        private Integer version;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Content {
        private List<ContentItem> content;
        private String type;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContentItem {
        private String text;
        private String type;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Printer {
        // id: 10195 (모든 프린터 / 공유 지원)
        // id: 10196 (모든 프린터 / 공유 미지원)
        // id: 10197 (기본 프린터 / 공유 지원)
        // id: 10198 (기본 프린터 / 공유 미지원)
        private String id;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Barcode {
        // id: 10199 (기본)
        // id: 10200 (3단)
        private String id;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Team {
        // id: dad204ef-b2e1-444e-accc-850560ea6550 (전자문서_PIO 2팀 3파트)
        private String id;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProjectProgressPhase {
        // id: 10211 (0. 사전 진행 단계)
        // id: 10212 (1. 프로젝트 시작)
        // id: 10214 (2. 개발서버 설치)
        // id: 10215 (3. 운영서버 설치)
        // id: 10216 (4. 프로젝트 안정화 기간)
        // id: 10217 (5. 프로젝트 종료)
        private String id;
    }

}
