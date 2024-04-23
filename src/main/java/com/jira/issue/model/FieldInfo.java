package com.jira.issue.model;

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
    JIRA_BASIC(FieldInfoCategory.BARCODE_TYPE, "기본", "10199"), // 기본
    JIRA_THREE_STEP(FieldInfoCategory.BARCODE_TYPE, "3단", "10200"), // 3단

    // 팀
    AIT_TEAM(FieldInfoCategory.TEAM, "기술본부 응용정보기술센터", "43364a3b-a84f-4fbf-9ce3-cfaa869b5654"), // 기술본부 응용정보기술센터
    AIT_TEAM_PART1(FieldInfoCategory.TEAM, "기술본부 응용정보기술센터 1파트", "6949b496-8918-4bdc-81a7-fb71193e8a4b"), // 기술본부 응용정보기술센터 1파트
    AIT_TEAM_PART2(FieldInfoCategory.TEAM, "기술본부 응용정보기술센터 2파트", "f866aef7-eed7-4c50-9aea-be7ce383c10c"), // 기술본부 응용정보기술센터 2파트
    AIT_TEAM_PART3(FieldInfoCategory.TEAM, "기술본부 응용정보기술센터 3파트", "dad204ef-b2e1-444e-accc-850560ea6550"), // 기술본부 응용정보기술센터 3파트

    // 파트
    PART_0(FieldInfoCategory.PART, "0", "10210"), // 0파트
    PART_1(FieldInfoCategory.PART, "1", "10207"), // 1파트
    PART_2(FieldInfoCategory.PART, "2", "10208"), // 2파트
    PART_3(FieldInfoCategory.PART, "3", "10209"), // 3파트

    // 멀티 OS
    MULTI_OS(FieldInfoCategory.OS, "Multi OS", "10467"),
    JIRA_MULTI_OS(FieldInfoCategory.OS, "지원", "10467"),

    // 프린터 지원 범위
    ALL_PRINTER_SHARED_PRINTER(FieldInfoCategory.PRINTER_SUPPORT_RANGE, "모두 지원/공유 지원", "10195"),
    ALL_PRINTER_NO_SHARED_PRINTER(FieldInfoCategory.PRINTER_SUPPORT_RANGE, "모두 지원/공유 불가", "10196"),
    DEFAULT_PRINTER_SHARED_PRINTER(FieldInfoCategory.PRINTER_SUPPORT_RANGE, "등록 지원/공유 지원", "10197"),
    DEFAULT_PRINTER_NO_SHARED_PRINTER(FieldInfoCategory.PRINTER_SUPPORT_RANGE, "등록 지원/공유 불가", "10198"),
    JIRA_ALL_PRINTER_SHARED_PRINTER(FieldInfoCategory.PRINTER_SUPPORT_RANGE, "모든 프린터 / 공유 지원", "10195"),
    JIRA_ALL_PRINTER_NO_SHARED_PRINTER(FieldInfoCategory.PRINTER_SUPPORT_RANGE, "모든 프린터 / 공유 미지원", "10196"),
    JIRA_DEFAULT_PRINTER_SHARED_PRINTER(FieldInfoCategory.PRINTER_SUPPORT_RANGE, "기본 프린터 / 공유 지원", "10197"),
    JIRA_DEFAULT_PRINTER_NO_SHARED_PRINTER(FieldInfoCategory.PRINTER_SUPPORT_RANGE, "기본 프린터 / 공유 미지원", "10198"),

    // 프로젝트 진행 단계
    PRE_PROJECT(FieldInfoCategory.PROJECT_PROGRESS_STEP, "0_9", "10211"), // 사전 진행 단계: 0, 9
    PROJECT_START(FieldInfoCategory.PROJECT_PROGRESS_STEP, "1", "10212"), // 프로젝트 시작: 1
    PROJECT_STABILIZATION(FieldInfoCategory.PROJECT_PROGRESS_STEP, "5_8", "10216"), // 프로젝트 안정화 기간: 5, 8
    PROJECT_END(FieldInfoCategory.PROJECT_PROGRESS_STEP, "92_93_94", "10217"), // 프로젝트 종료: 92, 93, 94
    JIRA_PRE_PROJECT(FieldInfoCategory.PROJECT_PROGRESS_STEP, "0. 사전 진행 단계", "10211"),
    JIRA_PROJECT_START(FieldInfoCategory.PROJECT_PROGRESS_STEP, "1. 프로젝트 시작", "10212"),
    JIRA_DEVELOPMENT_SERVER_INSTALL(FieldInfoCategory.PROJECT_PROGRESS_STEP, "2. 개발서버 설치", "10214"),
    JIRA_OPERATION_SERVER_INSTALL(FieldInfoCategory.PROJECT_PROGRESS_STEP, "3. 운영서버 설치", "10215"),
    JIRA_PROJECT_STABILIZATION(FieldInfoCategory.PROJECT_PROGRESS_STEP, "4. 프로젝트 안정화 기간", "10216"),
    JIRA_PROJECT_END(FieldInfoCategory.PROJECT_PROGRESS_STEP, "5. 프로젝트 종료", "10217"),

    // 계약 여부
    CONTRACTED(FieldInfoCategory.CONTRACT_STATUS, "1", "10201"),
    UNCONTRACTED(FieldInfoCategory.CONTRACT_STATUS, "0", "10202"),
    JIRA_CONTRACTED(FieldInfoCategory.CONTRACT_STATUS, "계약", "10201"),
    JIRA_UNCONTRACTED(FieldInfoCategory.CONTRACT_STATUS, "미계약", "10202"),
    JIRA_CONTRACT_TERMINATION(FieldInfoCategory.CONTRACT_STATUS, "계약 종료", "10479"),

    // 점검 방법
    VISIT_INSPECTION(FieldInfoCategory.INSPECTION_METHOD, "방문 점검", "10470"),
    REMOTE_INSPECTION(FieldInfoCategory.INSPECTION_METHOD, "원격 점검", "10471"),
    OTHER_INSPECTION(FieldInfoCategory.INSPECTION_METHOD, "기타", "10472"),
    SUPPORT_WHEN_FAIL(FieldInfoCategory.INSPECTION_METHOD, "장애시 지원", "10478"),

    // 점검 주기
    MONTH(FieldInfoCategory.INSPECTION_CYCLE, "월", "10473"),
    BIMONTHLY(FieldInfoCategory.INSPECTION_CYCLE, "격월", "10474"),
    QUARTER(FieldInfoCategory.INSPECTION_CYCLE, "분기", "10475"),
    HALF_YEAR(FieldInfoCategory.INSPECTION_CYCLE, "반기", "10476"),
    YEAR(FieldInfoCategory.INSPECTION_CYCLE, "년", "10477"),

    // transition
    COMPLETED(FieldInfoCategory.ISSUE_STATUS, "완료됨", "21"),

    // 제품 정보 1
    ePSZero1(FieldInfoCategory.PRODUCT_INFO1, "ePS Zero", "10510"),
    ePSNoaX1(FieldInfoCategory.PRODUCT_INFO1, "ePS NoaX", "10511"),
    ePSActiveX1(FieldInfoCategory.PRODUCT_INFO1, "ePS ActiveX", "10512"),
    ePSCustom1(FieldInfoCategory.PRODUCT_INFO1, "ePS Custom", "10513"),
    WebDRMZero1(FieldInfoCategory.PRODUCT_INFO1, "WebDRM Zero", "10514"),
    WebDRMNoaX1(FieldInfoCategory.PRODUCT_INFO1, "WebDRM NoaX", "10515"),
    WebDRMActiveX1(FieldInfoCategory.PRODUCT_INFO1, "WebDRM ActiveX", "10516"),
    WebDRMCustom1(FieldInfoCategory.PRODUCT_INFO1, "WebDRM Custom", "10517"),
    WebDRMforMobile1(FieldInfoCategory.PRODUCT_INFO1, "WebDRM for Mobile", "10518"),
    MaDMZero1(FieldInfoCategory.PRODUCT_INFO1, "MaDM Zero", "10519"),
    MaDMNoaX1(FieldInfoCategory.PRODUCT_INFO1, "MaDM NoaX", "10520"),
    MaDMActiveX1(FieldInfoCategory.PRODUCT_INFO1, "MaDM ActiveX", "10521"),
    MaDMCustom1(FieldInfoCategory.PRODUCT_INFO1, "MaDM Custom", "10522"),
    VoiceCodeforEPS1(FieldInfoCategory.PRODUCT_INFO1, "VoiceCode for ePS", "10523"),
    VoiceCodeforMaDM1(FieldInfoCategory.PRODUCT_INFO1, "VoiceCode for MaDM", "10524"),
    VoiceCodeforAPI1(FieldInfoCategory.PRODUCT_INFO1, "VoiceCode for API", "10525"),
    DocuDNAforEPS1(FieldInfoCategory.PRODUCT_INFO1, "DocuDNA for ePS", "10526"),
    DocuDNAforMaDM1(FieldInfoCategory.PRODUCT_INFO1, "DocuDNA for MaDM", "10527"),
    DocuDNACustom1(FieldInfoCategory.PRODUCT_INFO1, "DocuDNA Custom", "10528"),
    sDM1(FieldInfoCategory.PRODUCT_INFO1, "sDM", "10529"),
    OZ1(FieldInfoCategory.PRODUCT_INFO1, "OZ", "10531"),
    OZexe1(FieldInfoCategory.PRODUCT_INFO1, "OZ(exe)", "10532"),
    RD1(FieldInfoCategory.PRODUCT_INFO1, "RD", "10533"),
    RP1(FieldInfoCategory.PRODUCT_INFO1, "RP", "10534"),
    RPexe1(FieldInfoCategory.PRODUCT_INFO1, "RP(exe)", "10535"),
    UB1(FieldInfoCategory.PRODUCT_INFO1, "UB", "10536"),
    UBStorm1(FieldInfoCategory.PRODUCT_INFO1, "UBStorm", "10537"),
    AI1(FieldInfoCategory.PRODUCT_INFO1, "AI", "10538"),
    H2O1(FieldInfoCategory.PRODUCT_INFO1, "H2O", "10539"),
    HTML1(FieldInfoCategory.PRODUCT_INFO1, "HTML", "10540"),
    HTMLMail1(FieldInfoCategory.PRODUCT_INFO1, "HTML(Mail)", "10541"),
    Unidocs1(FieldInfoCategory.PRODUCT_INFO1, "Unidocs", "10542"),
    Epapyrus1(FieldInfoCategory.PRODUCT_INFO1, "Epapyrus", "10543"),
    PDF1(FieldInfoCategory.PRODUCT_INFO1, "PDF", "10544"),
    PDFMail1(FieldInfoCategory.PRODUCT_INFO1, "PDF(Mail)", "10545"),
    VC1(FieldInfoCategory.PRODUCT_INFO1, "VC", "10547"),
    RMS1(FieldInfoCategory.PRODUCT_INFO1, "RMS", "10548"),
    WaterMark1(FieldInfoCategory.PRODUCT_INFO1, "WaterMark", "10549"),
    VDI1(FieldInfoCategory.PRODUCT_INFO1, "VDI", "10716"),
    VPN1(FieldInfoCategory.PRODUCT_INFO1, "VPN", "10717"),

    // 제품 정보 2
    ePSZero2(FieldInfoCategory.PRODUCT_INFO2, "ePS Zero", "10551"),
    ePSNoaX2(FieldInfoCategory.PRODUCT_INFO2, "ePS NoaX", "10552"),
    ePSActiveX2(FieldInfoCategory.PRODUCT_INFO2, "ePS ActiveX", "10553"),
    ePSCustom2(FieldInfoCategory.PRODUCT_INFO2, "ePS Custom", "10554"),
    WebDRMZero2(FieldInfoCategory.PRODUCT_INFO2, "WebDRM Zero", "10555"),
    WebDRMNoaX2(FieldInfoCategory.PRODUCT_INFO2, "WebDRM NoaX", "10556"),
    WebDRMActiveX2(FieldInfoCategory.PRODUCT_INFO2, "WebDRM ActiveX", "10557"),
    WebDRMCustom2(FieldInfoCategory.PRODUCT_INFO2, "WebDRM Custom", "10558"),
    WebDRMforMobile2(FieldInfoCategory.PRODUCT_INFO2, "WebDRM for Mobile", "10559"),
    MaDMZero2(FieldInfoCategory.PRODUCT_INFO2, "MaDM Zero", "10560"),
    MaDMNoaX2(FieldInfoCategory.PRODUCT_INFO2, "MaDM NoaX", "10561"),
    MaDMActiveX2(FieldInfoCategory.PRODUCT_INFO2, "MaDM ActiveX", "10562"),
    MaDMCustom2(FieldInfoCategory.PRODUCT_INFO2, "MaDM Custom", "10563"),
    VoiceCodeforEPS2(FieldInfoCategory.PRODUCT_INFO2, "VoiceCode for ePS", "10564"),
    VoiceCodeforMaDM2(FieldInfoCategory.PRODUCT_INFO2, "VoiceCode for MaDM", "10565"),
    VoiceCodeforAPI2(FieldInfoCategory.PRODUCT_INFO2, "VoiceCode for API", "10566"),
    DocuDNAforEPS2(FieldInfoCategory.PRODUCT_INFO2, "DocuDNA for ePS", "10567"),
    DocuDNAforMaDM2(FieldInfoCategory.PRODUCT_INFO2, "DocuDNA for MaDM", "10568"),
    DocuDNACustom2(FieldInfoCategory.PRODUCT_INFO2, "DocuDNA Custom", "10569"),
    sDM2(FieldInfoCategory.PRODUCT_INFO2, "sDM", "10570"),
    OZ2(FieldInfoCategory.PRODUCT_INFO2, "OZ", "10572"),
    OZexe2(FieldInfoCategory.PRODUCT_INFO2, "OZ(exe)", "10573"),
    RD2(FieldInfoCategory.PRODUCT_INFO2, "RD", "10574"),
    RP2(FieldInfoCategory.PRODUCT_INFO2, "RP", "10575"),
    RPexe2(FieldInfoCategory.PRODUCT_INFO2, "RP(exe)", "10576"),
    UB2(FieldInfoCategory.PRODUCT_INFO2, "UB", "10577"),
    UBStorm2(FieldInfoCategory.PRODUCT_INFO2, "UBStorm", "10578"),
    AI2(FieldInfoCategory.PRODUCT_INFO2, "AI", "10579"),
    H2O2(FieldInfoCategory.PRODUCT_INFO2, "H2O", "10580"),
    HTML2(FieldInfoCategory.PRODUCT_INFO2, "HTML", "10581"),
    HTMLMail2(FieldInfoCategory.PRODUCT_INFO2, "HTML(Mail)", "10582"),
    Unidocs2(FieldInfoCategory.PRODUCT_INFO2, "Unidocs", "10583"),
    Epapyrus2(FieldInfoCategory.PRODUCT_INFO2, "Epapyrus", "10584"),
    PDF2(FieldInfoCategory.PRODUCT_INFO2, "PDF", "10585"),
    PDFMail2(FieldInfoCategory.PRODUCT_INFO2, "PDF(Mail)", "10586"),
    VC2(FieldInfoCategory.PRODUCT_INFO2, "VC", "10588"),
    RMS2(FieldInfoCategory.PRODUCT_INFO2, "RMS", "10589"),
    WaterMark2(FieldInfoCategory.PRODUCT_INFO2, "WaterMark", "10590"),
    VDI2(FieldInfoCategory.PRODUCT_INFO2, "VDI", "10718"),
    VPN2(FieldInfoCategory.PRODUCT_INFO2, "VPN", "10719"),

    // 제품 정보 3
    ePSZero3(FieldInfoCategory.PRODUCT_INFO3, "ePS Zero", "10592"),
    ePSNoaX3(FieldInfoCategory.PRODUCT_INFO3, "ePS NoaX", "10593"),
    ePSActiveX3(FieldInfoCategory.PRODUCT_INFO3, "ePS ActiveX", "10594"),
    ePSCustom3(FieldInfoCategory.PRODUCT_INFO3, "ePS Custom", "10595"),
    WebDRMZero3(FieldInfoCategory.PRODUCT_INFO3, "WebDRM Zero", "10596"),
    WebDRMNoaX3(FieldInfoCategory.PRODUCT_INFO3, "WebDRM NoaX", "10597"),
    WebDRMActiveX3(FieldInfoCategory.PRODUCT_INFO3, "WebDRM ActiveX", "10598"),
    WebDRMCustom3(FieldInfoCategory.PRODUCT_INFO3, "WebDRM Custom", "10599"),
    WebDRMforMobile3(FieldInfoCategory.PRODUCT_INFO3, "WebDRM for Mobile", "10600"),
    MaDMZero3(FieldInfoCategory.PRODUCT_INFO3, "MaDM Zero", "10601"),
    MaDMNoaX3(FieldInfoCategory.PRODUCT_INFO3, "MaDM NoaX", "10602"),
    MaDMActiveX3(FieldInfoCategory.PRODUCT_INFO3, "MaDM ActiveX", "10603"),
    MaDMCustom3(FieldInfoCategory.PRODUCT_INFO3, "MaDM Custom", "10604"),
    VoiceCodeforEPS3(FieldInfoCategory.PRODUCT_INFO3, "VoiceCode for ePS", "10605"),
    VoiceCodeforMaDM3(FieldInfoCategory.PRODUCT_INFO3, "VoiceCode for MaDM", "10606"),
    VoiceCodeforAPI3(FieldInfoCategory.PRODUCT_INFO3, "VoiceCode for API", "10607"),
    DocuDNAforEPS3(FieldInfoCategory.PRODUCT_INFO3, "DocuDNA for ePS", "10608"),
    DocuDNAforMaDM3(FieldInfoCategory.PRODUCT_INFO3, "DocuDNA for MaDM", "10609"),
    DocuDNACustom3(FieldInfoCategory.PRODUCT_INFO3, "DocuDNA Custom", "10610"),
    sDM3(FieldInfoCategory.PRODUCT_INFO3, "sDM", "10611"),
    OZ3(FieldInfoCategory.PRODUCT_INFO3, "OZ", "10613"),
    OZexe3(FieldInfoCategory.PRODUCT_INFO3, "OZ(exe)", "10614"),
    RD3(FieldInfoCategory.PRODUCT_INFO3, "RD", "10615"),
    RP3(FieldInfoCategory.PRODUCT_INFO3, "RP", "10616"),
    RPexe3(FieldInfoCategory.PRODUCT_INFO3, "RP(exe)", "10617"),
    UB3(FieldInfoCategory.PRODUCT_INFO3, "UB", "10618"),
    UBStorm3(FieldInfoCategory.PRODUCT_INFO3, "UBStorm", "10619"),
    AI3(FieldInfoCategory.PRODUCT_INFO3, "AI", "10620"),
    H2O3(FieldInfoCategory.PRODUCT_INFO3, "H2O", "10621"),
    HTML3(FieldInfoCategory.PRODUCT_INFO3, "HTML", "10622"),
    HTMLMail3(FieldInfoCategory.PRODUCT_INFO3, "HTML(Mail)", "10623"),
    Unidocs3(FieldInfoCategory.PRODUCT_INFO3, "Unidocs", "10624"),
    Epapyrus3(FieldInfoCategory.PRODUCT_INFO3, "Epapyrus", "10625"),
    PDF3(FieldInfoCategory.PRODUCT_INFO3, "PDF", "10626"),
    PDFMail3(FieldInfoCategory.PRODUCT_INFO3, "PDF(Mail)", "10627"),
    VC3(FieldInfoCategory.PRODUCT_INFO3, "VC", "10629"),
    RMS3(FieldInfoCategory.PRODUCT_INFO3, "RMS", "10630"),
    WaterMark3(FieldInfoCategory.PRODUCT_INFO3, "WaterMark", "10631"),
    VDI3(FieldInfoCategory.PRODUCT_INFO3, "VDI", "10720"),
    VPN3(FieldInfoCategory.PRODUCT_INFO3, "VPN", "10721"),

    // 제품 정보 4
    ePSZero4(FieldInfoCategory.PRODUCT_INFO4, "ePS Zero", "10633"),
    ePSNoaX4(FieldInfoCategory.PRODUCT_INFO4, "ePS NoaX", "10634"),
    ePSActiveX4(FieldInfoCategory.PRODUCT_INFO4, "ePS ActiveX", "10635"),
    ePSCustom4(FieldInfoCategory.PRODUCT_INFO4, "ePS Custom", "10636"),
    WebDRMZero4(FieldInfoCategory.PRODUCT_INFO4, "WebDRM Zero", "10637"),
    WebDRMNoaX4(FieldInfoCategory.PRODUCT_INFO4, "WebDRM NoaX", "10638"),
    WebDRMActiveX4(FieldInfoCategory.PRODUCT_INFO4, "WebDRM ActiveX", "10639"),
    WebDRMCustom4(FieldInfoCategory.PRODUCT_INFO4, "WebDRM Custom", "10640"),
    WebDRMforMobile4(FieldInfoCategory.PRODUCT_INFO4, "WebDRM for Mobile", "10641"),
    MaDMZero4(FieldInfoCategory.PRODUCT_INFO4, "MaDM Zero", "10642"),
    MaDMNoaX4(FieldInfoCategory.PRODUCT_INFO4, "MaDM NoaX", "10643"),
    MaDMActiveX4(FieldInfoCategory.PRODUCT_INFO4, "MaDM ActiveX", "10644"),
    MaDMCustom4(FieldInfoCategory.PRODUCT_INFO4, "MaDM Custom", "10645"),
    VoiceCodeforEPS4(FieldInfoCategory.PRODUCT_INFO4, "VoiceCode for ePS", "10646"),
    VoiceCodeforMaDM4(FieldInfoCategory.PRODUCT_INFO4, "VoiceCode for MaDM", "10647"),
    VoiceCodeforAPI4(FieldInfoCategory.PRODUCT_INFO4, "VoiceCode for API", "10648"),
    DocuDNAforEPS4(FieldInfoCategory.PRODUCT_INFO4, "DocuDNA for ePS", "10649"),
    DocuDNAforMaDM4(FieldInfoCategory.PRODUCT_INFO4, "DocuDNA for MaDM", "10650"),
    DocuDNACustom4(FieldInfoCategory.PRODUCT_INFO4, "DocuDNA Custom", "10651"),
    sDM4(FieldInfoCategory.PRODUCT_INFO4, "sDM", "10652"),
    OZ4(FieldInfoCategory.PRODUCT_INFO4, "OZ", "10654"),
    OZexe4(FieldInfoCategory.PRODUCT_INFO4, "OZ(exe)", "10655"),
    RD4(FieldInfoCategory.PRODUCT_INFO4, "RD", "10656"),
    RP4(FieldInfoCategory.PRODUCT_INFO4, "RP", "10657"),
    RPexe4(FieldInfoCategory.PRODUCT_INFO4, "RP(exe)", "10658"),
    UB4(FieldInfoCategory.PRODUCT_INFO4, "UB", "10659"),
    UBStorm4(FieldInfoCategory.PRODUCT_INFO4, "UBStorm", "10660"),
    AI4(FieldInfoCategory.PRODUCT_INFO4, "AI", "10661"),
    H2O4(FieldInfoCategory.PRODUCT_INFO4, "H2O", "10662"),
    HTML4(FieldInfoCategory.PRODUCT_INFO4, "HTML", "10663"),
    HTMLMail4(FieldInfoCategory.PRODUCT_INFO4, "HTML(Mail)", "10664"),
    Unidocs4(FieldInfoCategory.PRODUCT_INFO4, "Unidocs", "10665"),
    Epapyrus4(FieldInfoCategory.PRODUCT_INFO4, "Epapyrus", "10666"),
    PDF4(FieldInfoCategory.PRODUCT_INFO4, "PDF", "10667"),
    PDFMail4(FieldInfoCategory.PRODUCT_INFO4, "PDF(Mail)", "10668"),
    VC4(FieldInfoCategory.PRODUCT_INFO4, "VC", "10670"),
    RMS4(FieldInfoCategory.PRODUCT_INFO4, "RMS", "10671"),
    WaterMark4(FieldInfoCategory.PRODUCT_INFO4, "WaterMark", "10672"),
    VDI4(FieldInfoCategory.PRODUCT_INFO4, "VDI", "10722"),
    VPN4(FieldInfoCategory.PRODUCT_INFO4, "VPN", "10723"),

    // 제품 정보 5
    ePSZero5(FieldInfoCategory.PRODUCT_INFO5, "ePS Zero", "10674"),
    ePSNoaX5(FieldInfoCategory.PRODUCT_INFO5, "ePS NoaX", "10675"),
    ePSActiveX5(FieldInfoCategory.PRODUCT_INFO5, "ePS ActiveX", "10676"),
    ePSCustom5(FieldInfoCategory.PRODUCT_INFO5, "ePS Custom", "10677"),
    WebDRMZero5(FieldInfoCategory.PRODUCT_INFO5, "WebDRM Zero", "10678"),
    WebDRMNoaX5(FieldInfoCategory.PRODUCT_INFO5, "WebDRM NoaX", "10679"),
    WebDRMActiveX5(FieldInfoCategory.PRODUCT_INFO5, "WebDRM ActiveX", "10680"),
    WebDRMCustom5(FieldInfoCategory.PRODUCT_INFO5, "WebDRM Custom", "10681"),
    WebDRMforMobile5(FieldInfoCategory.PRODUCT_INFO5, "WebDRM for Mobile", "10682"),
    MaDMZero5(FieldInfoCategory.PRODUCT_INFO5, "MaDM Zero", "10683"),
    MaDMNoaX5(FieldInfoCategory.PRODUCT_INFO5, "MaDM NoaX", "10684"),
    MaDMActiveX5(FieldInfoCategory.PRODUCT_INFO5, "MaDM ActiveX", "10685"),
    MaDMCustom5(FieldInfoCategory.PRODUCT_INFO5, "MaDM Custom", "10686"),
    VoiceCodeforEPS5(FieldInfoCategory.PRODUCT_INFO5, "VoiceCode for ePS", "10687"),
    VoiceCodeforMaDM5(FieldInfoCategory.PRODUCT_INFO5, "VoiceCode for MaDM", "10688"),
    VoiceCodeforAPI5(FieldInfoCategory.PRODUCT_INFO5, "VoiceCode for API", "10689"),
    DocuDNAforEPS5(FieldInfoCategory.PRODUCT_INFO5, "DocuDNA for ePS", "10690"),
    DocuDNAforMaDM5(FieldInfoCategory.PRODUCT_INFO5, "DocuDNA for MaDM", "10691"),
    DocuDNACustom5(FieldInfoCategory.PRODUCT_INFO5, "DocuDNA Custom", "10692"),
    sDM5(FieldInfoCategory.PRODUCT_INFO5, "sDM", "10693"),
    OZ5(FieldInfoCategory.PRODUCT_INFO5, "OZ", "10695"),
    OZexe5(FieldInfoCategory.PRODUCT_INFO5, "OZ(exe)", "10696"),
    RD5(FieldInfoCategory.PRODUCT_INFO5, "RD", "10697"),
    RP5(FieldInfoCategory.PRODUCT_INFO5, "RP", "10698"),
    RPexe5(FieldInfoCategory.PRODUCT_INFO5, "RP(exe)", "10699"),
    UB5(FieldInfoCategory.PRODUCT_INFO5, "UB", "10700"),
    UBStorm5(FieldInfoCategory.PRODUCT_INFO5, "UBStorm", "10701"),
    AI5(FieldInfoCategory.PRODUCT_INFO5, "AI", "10702"),
    H2O5(FieldInfoCategory.PRODUCT_INFO5, "H2O", "10703"),
    HTML5(FieldInfoCategory.PRODUCT_INFO5, "HTML", "10704"),
    HTMLMail5(FieldInfoCategory.PRODUCT_INFO5, "HTML(Mail)", "10705"),
    Unidocs5(FieldInfoCategory.PRODUCT_INFO5, "Unidocs", "10706"),
    Epapyrus5(FieldInfoCategory.PRODUCT_INFO5, "Epapyrus", "10707"),
    PDF5(FieldInfoCategory.PRODUCT_INFO5, "PDF", "10708"),
    PDFMail5(FieldInfoCategory.PRODUCT_INFO5, "PDF(Mail)", "10709"),
    VC5(FieldInfoCategory.PRODUCT_INFO5, "VC", "10711"),
    RMS5(FieldInfoCategory.PRODUCT_INFO5, "RMS", "10712"),
    WaterMark5(FieldInfoCategory.PRODUCT_INFO5, "WaterMark", "10713"),
    VDI5(FieldInfoCategory.PRODUCT_INFO5, "VDI", "10714"),
    VPN5(FieldInfoCategory.PRODUCT_INFO5, "VPN", "10715");

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
