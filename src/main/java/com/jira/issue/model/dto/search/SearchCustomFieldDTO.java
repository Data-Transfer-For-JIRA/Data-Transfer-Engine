package com.jira.issue.model.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jira.issue.model.dto.FieldDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchCustomFieldDTO extends FieldDTO{

    // 공통 커스텀 필드
    @JsonProperty("customfield_10275")
    private FieldDTO.User salesManager; // 영업 담당자

    @JsonProperty("customfield_10270")
    private String contractor; // 계약사

    @JsonProperty("customfield_10271")
    private String client; // 고객사

    @JsonProperty("customfield_10272")
    private FieldDTO.Field barcodeType; // 바코드 타입

    @JsonProperty("customfield_10001")
    private Team team; // 팀

    @JsonProperty("customfield_10279")
    private FieldDTO.Field part; // 파트

    @JsonProperty("customfield_10269")
    private FieldDTO.User subAssignee; // 부 담당자

    @JsonProperty("customfield_10415")
    private List<FieldDTO.Field> multiOsSupport; // 멀티 OS 정보

    @JsonProperty("customfield_10247")
    private FieldDTO.Field printerSupportRange; // 프린터 지원 여부

    // 제품 정보
    @JsonProperty("customfield_10445")
    private List<FieldDTO.Field> productInfo1;

    @JsonProperty("customfield_10446")
    private List<FieldDTO.Field> productInfo2;

    @JsonProperty("customfield_10447")
    private List<FieldDTO.Field> productInfo3;

    @JsonProperty("customfield_10448")
    private List<FieldDTO.Field> productInfo4;

    @JsonProperty("customfield_10449")
    private List<FieldDTO.Field> productInfo5;

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Team {
        String name;
        String id;
    }
}
