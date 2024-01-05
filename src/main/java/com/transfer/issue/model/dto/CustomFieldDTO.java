package com.transfer.issue.model.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomFieldDTO extends FieldDTO {

    // 공통 커스텀 필드
    @JsonProperty("customfield_10275")
    private User salesManager;

    @JsonProperty("customfield_10270")
    private String contractor;

    @JsonProperty("customfield_10271")
    private String client;

    @JsonProperty("customfield_10277")
    private List<Field> productType;

    @JsonProperty("customfield_10406")
    private List<Field> productInfo;

    @JsonProperty("customfield_10408")
    private List<Field> linkInfo;

    @JsonProperty("customfield_10272")
    private Field barcodeType;

    @JsonProperty("customfield_10001")
    private String team;

    @JsonProperty("customfield_10279")
    private Field part;

    @JsonProperty("customfield_10269")
    private User subAssignee;

    @JsonProperty("customfield_10416")
    private String productTypeEtc;

    @JsonProperty("customfield_10407")
    private String productInfoEtc;

    @JsonProperty("customfield_10409")
    private String linkInfoEtc;

    @JsonProperty("customfield_10415")
    private List<Field> multiOsSupport;

    @JsonProperty("customfield_10247")
    private Field printerSupportRange;

    @JsonProperty("customfield_10442")
    private Description etc;

    // ======================== 변경 필드 ========================
    @JsonProperty("customfield_10445")
    private List<Field> productInfo1;

    @JsonProperty("customfield_10446")
    private List<Field> productInfo2;

    @JsonProperty("customfield_10447")
    private List<Field> productInfo3;

    @JsonProperty("customfield_10448")
    private List<Field> productInfo4;

    @JsonProperty("customfield_10449")
    private List<Field> productInfo5;
}
