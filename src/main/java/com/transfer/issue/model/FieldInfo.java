package com.transfer.issue.model;

import java.util.Arrays;

public enum FieldInfo {

    // 이슈타입
    PROJECT(FieldInfoCategory.ISSUE_TYPE, "프로젝트 기본 정보", "10441"),
    MAINTENANCE(FieldInfoCategory.ISSUE_TYPE, "유지보수 기본 정보", "10442"),
    TASK(FieldInfoCategory.ISSUE_TYPE, "작업", "10002"),

    // 제품 유형
    EPAGESAFER(FieldInfoCategory.PRODUCT_TYPE, "e-PageSAFER", "10205"),
    WEBDRM(FieldInfoCategory.PRODUCT_TYPE, "WebDRM", "10206"),
    MDM(FieldInfoCategory.PRODUCT_TYPE, "MDM", "10468"),
    OTHER_PRODUCT_TYPE(FieldInfoCategory.PRODUCT_TYPE, "기타", "10469"),

    // 제품 정보
    ZEROCLIENT(FieldInfoCategory.PRODUCT_INFO, "12", "10450"),
    NON_ACTIVEX(FieldInfoCategory.PRODUCT_INFO, "11", "10451"),
    ACTIVEX(FieldInfoCategory.PRODUCT_INFO, "10", "10452"),
    OTHER_PRODUCT_INFO(FieldInfoCategory.PRODUCT_INFO, "기타", "10453"),

    // 연동 정보
    OZ(FieldInfoCategory.LINK_INFO, "OZ", "10454"),
    RP(FieldInfoCategory.LINK_INFO, "RP", "10455"),
    RD(FieldInfoCategory.LINK_INFO, "RD", "10456"),
    UB(FieldInfoCategory.LINK_INFO, "UB", "10457"),
    AI(FieldInfoCategory.LINK_INFO, "AI", "10458"),
    EP(FieldInfoCategory.LINK_INFO, "EP", "10459"),
    VC(FieldInfoCategory.LINK_INFO, "VC", "10466"),
    HTML(FieldInfoCategory.LINK_INFO, "HTML", "10460"),
    MAIL(FieldInfoCategory.LINK_INFO, "MAIL", "10461"),
    PDF(FieldInfoCategory.LINK_INFO, "PDF", "10462"),
    OTHER_CONNECTION_INFO(FieldInfoCategory.LINK_INFO, "기타", "10463"),

    // 바코드 타입
    BASIC(FieldInfoCategory.BARCODE_TYPE, "0", "10199"), // 기본
    THREE_STEP(FieldInfoCategory.BARCODE_TYPE, "1", "10200"), // 3단

    // 팀
    EDOC_PIO_TEAM1(FieldInfoCategory.TEAM, "전자문서 PIO 1팀", "958ac74d-b505-42bd-951d-d33cd54a4db8"), // 전자문서_PIO 1팀
    EDOC_PIO_TEAM2(FieldInfoCategory.TEAM, "전자문서 PIO 2팀", "563a40c8-3310-4acf-995f-c5e912d662f8"), // 전자문서_PIO 2팀
    EDOC_PIO_TEAM2_PART1(FieldInfoCategory.TEAM, "전자문서 PIO 2팀 1파트", "6949b496-8918-4bdc-81a7-fb71193e8a4b"), // 전자문서_PIO 2팀 1파트
    EDOC_PIO_TEAM2_PART2(FieldInfoCategory.TEAM, "전자문서 PIO 2팀 2파트", "f866aef7-eed7-4c50-9aea-be7ce383c10c"), // 전자문서_PIO 2팀 2파트
    EDOC_PIO_TEAM2_PART3(FieldInfoCategory.TEAM, "전자문서 PIO 2팀 3파트", "dad204ef-b2e1-444e-accc-850560ea6550"), // 전자문서_PIO 2팀 3파트

    // 파트
    PART_0(FieldInfoCategory.PART, "0", "10210"), // 0파트
    PART_1(FieldInfoCategory.PART, "1", "10207"), // 1파트
    PART_2(FieldInfoCategory.PART, "2", "10208"), // 2파트
    PART_3(FieldInfoCategory.PART, "3", "10209"), // 3파트

    // 멀티 OS
    MULTI_OS(FieldInfoCategory.OS, "Multi OS", "10467"),

    // 프린터 지원 범위
    ALL_PRINTER_SHARED_PRINTER(FieldInfoCategory.PRINTER_SUPPORT_RANGE, "모두 지원/공유 지원", "10195"),
    ALL_PRINTER_NO_SHARED_PRINTER(FieldInfoCategory.PRINTER_SUPPORT_RANGE, "모두 지원/공유 불가", "10196"),
    DEFAULT_PRINTER_SHARED_PRINTER(FieldInfoCategory.PRINTER_SUPPORT_RANGE, "등록 지원/공유 지원", "10197"),
    DEFAULT_PRINTER_NO_SHARED_PRINTER(FieldInfoCategory.PRINTER_SUPPORT_RANGE, "등록 지원/공유 불가", "10198"),

    // 프로젝트 진행 단계
    PRE_PROJECT(FieldInfoCategory.PROJECT_PROGRESS_STEP, "0_9", "10211"), // 사전 진행 단계: 0, 9
    PROJECT_START(FieldInfoCategory.PROJECT_PROGRESS_STEP, "1", "10212"), // 프로젝트 시작: 1
    DEVELOPMENT_SERVER_INSTALL(FieldInfoCategory.PROJECT_PROGRESS_STEP, "2. 개발서버 설치", "10214"),
    OPERATION_SERVER_INSTALL(FieldInfoCategory.PROJECT_PROGRESS_STEP, "3. 운영서버 설치", "10215"),
    PROJECT_STABILIZATION(FieldInfoCategory.PROJECT_PROGRESS_STEP, "5_8", "10216"), // 프로젝트 안정화 기간: 5, 8
    PROJECT_END(FieldInfoCategory.PROJECT_PROGRESS_STEP, "92_93_94", "10217"), // 프로젝트 종료: 92, 93, 94

    // 계약 여부
    CONTRACTED(FieldInfoCategory.CONTRACT_STATUS, "1", "10201"),
    NON_CONTRACT(FieldInfoCategory.CONTRACT_STATUS, "0", "10202"),

    // 점검 방법
    VISIT_INSPECTION(FieldInfoCategory.INSPECTION_METHOD, "방문", "10470"),
    REMOTE_INSPECTION(FieldInfoCategory.INSPECTION_METHOD, "원격", "10471"),
    OTHER_INSPECTION(FieldInfoCategory.INSPECTION_METHOD, "기타", "10472"),
    SUPPORT_WHEN_FAIL(FieldInfoCategory.INSPECTION_METHOD, "장애시 지원", "10478"),

    // 점검 주기
    MONTH(FieldInfoCategory.INSPECTION_CYCLE, "월", "10473"),
    BIMONTHLY(FieldInfoCategory.INSPECTION_CYCLE, "격월", "10474"),
    QUARTER(FieldInfoCategory.INSPECTION_CYCLE, "분기", "10475"),
    HALF_YEAR(FieldInfoCategory.INSPECTION_CYCLE, "반기", "10476"),
    YEAR(FieldInfoCategory.INSPECTION_CYCLE, "년", "10477"),

    // transition
    COMPLETED(FieldInfoCategory.ISSUE_STATUS, "완료됨", "21");

    private final String category;
    private final String label;
    private final String id;

    FieldInfo(String category, String label, String id) {
        this.category = category;
        this.label = label;
        this.id = id;
    }

    public static FieldInfo ofLabel(String category, String label) {
        return Arrays.stream(FieldInfo.values())
                .filter(field -> field.category.equals(category) && field.label.equals(label))
                .findFirst()
                .orElse(null);
    }

    public String getId() {
        return id;
    }

    public static String getIdByCategoryAndLabel(String inputCategory, String inputLabel) {
        for (FieldInfo info : values()) {
            if (info.category.equals(inputCategory)) {
                String[] labels = info.label.split("_");
                for (String label : labels) {
                    if (label.equals(inputLabel)) {
                        return info.id;
                    }
                }
            }
        }
        return null;
    }
}
