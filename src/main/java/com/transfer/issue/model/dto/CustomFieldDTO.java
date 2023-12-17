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
    //@JsonProperty("sales_manager")
    private User customfield_10275;

    //@JsonProperty("contractor")
    private String customfield_10270;

    //@JsonProperty("client")
    private String customfield_10271;

    //@JsonProperty("product_type")
    private List<Field> customfield_10277;

    //@JsonProperty("product_info")
    private List<Field> customfield_10406;

    //@JsonProperty("link_info")
    private List<Field> customfield_10408;

    //@JsonProperty("barcode_type")
    private Field customfield_10272;

    //@JsonProperty("team")
    private String customfield_10001;

    //@JsonProperty("part")
    private Field customfield_10279;

    //@JsonProperty("sub_engineer")
    private User customfield_10269;

    //@JsonProperty("product_type_etc")
    private String customfield_10416;

    //@JsonProperty("product_info_etc")
    private String customfield_10407;

    //@JsonProperty("link_info_etc")
    private String customfield_10409;

    //@JsonProperty("multi_os_support")
    private List<Field> customfield_10415;

    //@JsonProperty("printer_support_range")
    private Field customfield_10247;

}
