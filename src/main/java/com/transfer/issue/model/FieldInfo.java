package com.transfer.issue.model;

import java.util.Arrays;

public enum FieldInfo {

    // 이슈타입
    PROJECT("프로젝트 기본정보", 10441),
    MAINTENANCE("유지보수 기본정보", 10442),
    TASK("작업", 10002),

    // 제품 유형
    EPAGESAFER("e-PageSAFER", 10205),
    WEBDRM("WebDRM", 10206),
    MDM("MDM", 10468),
    OTHER_PRODUCT_TYPE("기타", 10469),

    // 제품 정보
    ZEROCLIENT("ZeroClient", 10450),
    NON_ACTIVEX("Non-ActiveX", 10451),
    ACTIVEX("ActiveX", 10452),
    OTHER_PRODUCT_INFO("기타", 10453),

    // 연동 정보
    OZ("OZ", 10454),
    RP("RP", 10455),
    RD("RD", 10456),
    UB("UB", 10457),
    AI("AI", 10458),
    EP("EP", 10459),
    VC("VC", 10466),
    HTML("HTML", 10460),
    MAIL("MAIL", 10461),
    PDF("PDF", 10462),
    OTHER_CONNECTION_INFO("기타", 10463),

    // 바코드 타입
    BASIC("기본", 10199),
    THREE_STEP("3단", 10200),

    // 프로젝트 진행 단계
    PRE_PROJECT("0. 사전 진행 단계", 10211),
    PROJECT_START("1. 프로젝트 시작", 10212),
    DEVELOPMENT_SERVER_INSTALL("2. 개발서버 설치", 10214),
    OPERATION_SERVER_INSTALL("3. 운영서버 설치", 10215),
    PROJECT_STABILIZATION("4. 프로젝트 안정화 기간", 10216),
    PROJECT_END("5. 프로젝트 종료", 10217),

    // 계약 여부
    CONTRACTED("계약", 10201),
    NON_CONTRACT("미계약", 10202),

    // 점검 방법
    VISIT_INSPECTION("방문 점검", 10470),
    REMOTE_INSPECTION("원격 점검", 10471),
    OTHER_INSPECTION("기타", 10472),
    SUPPORT_WHEN_FAIL("장애시 지원", 10478),

    // 점검 주기
    MONTH("월", 10473),
    BIMONTHLY("격월", 10474),
    QUARTER("분기", 10475),
    HALF_YEAR("반기", 10476),
    YEAR("년", 10477);

    private final String label;
    private final int id;

    FieldInfo(String label, int id) {
        this.label = label;
        this.id = id;
    }

    public static FieldInfo ofLabel(String label) {
        return Arrays.stream(FieldInfo.values())
                .filter(field -> field.label.equals(label))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public int getId() {
        return id;
    }
}
