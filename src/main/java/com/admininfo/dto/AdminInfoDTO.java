package com.admininfo.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AdminInfoDTO {

    private Integer personalId;

    private String id;

    private String token;

    private String url;
}
