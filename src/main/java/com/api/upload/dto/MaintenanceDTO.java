package com.api.upload.dto;

import lombok.*;

import java.util.Date;
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceDTO {

    String 품의번호;
    String 회계연도;
    String 월;
    String 년월;
    String 유형;
    String 발주자;
    String 계약자;
    String 프로젝트코드;
    String 프로젝트명칭;
    String 영업담당;
    String 계약시작;
    String 계약종료;
}
