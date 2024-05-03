package com.api.upload.service;

import com.api.platform.dto.BaseDTO;
import com.api.platform.service.PlatformProjectImpl;
import com.api.upload.dto.MaintenanceDTO;
import com.jira.issue.service.JiraIssue;
import com.jira.project.model.dao.TB_JML_JpaRepository;
import com.jira.project.model.entity.TB_JML_Entity;
import com.utils.SaveLog;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@AllArgsConstructor
@Service("UploadService")
public class UploadServiceImpl implements UploadService {

    @Autowired
    TB_JML_JpaRepository TB_JML_JpaRepository;

    @Autowired
    JiraIssue jiraIssue;

    @Autowired
    PlatformProjectImpl platformProject;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DateTimeFormatter 목표_포맷 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String uploadMaintenanceDate(String fileName, String filePath) throws Exception {

        // 파일 읽기
        List<MaintenanceDTO> 파일_데이터 = readXlsxFile(fileName, filePath);

        int failCount = 0;
        int successCount = 0;
        String 메시지 = null;
        for(MaintenanceDTO 행_데이터 : 파일_데이터){

            String 프로젝트_코드 = 행_데이터.get프로젝트코드();
            String 프로젝트명 = 행_데이터.get프로젝트명칭();
            Optional<TB_JML_Entity> 프로젝트_정보 = Optional.ofNullable(TB_JML_JpaRepository.findByProjectCode(프로젝트_코드));

            String 이슈키 = 프로젝트_정보
                    .map(프로젝트 -> jiraIssue.getBaseIssueKeyByJiraKey(프로젝트.getKey()))
                    .orElse("");

            if (!StringUtils.isEmpty(이슈키)) {
                logger.info("[:: UploadServiceImpl ::] 엑셀파일 -> 이슈키 : " + 이슈키 + " - " + 행_데이터);

                // 기본 정보 이슈 업데이트
                BaseDTO.EssentialDTO essentialDTO = BaseDTO.EssentialDTO.builder()
                        .projectFlag("M")
                        .build();

                BaseDTO.CommonDTO commonDTO = BaseDTO.CommonDTO.builder()
                        .client(행_데이터.get발주자())
                        .contractor(행_데이터.get계약자())
                        .salesManager(행_데이터.get영업담당())
                        .build();

                BaseDTO.SelectedDTO selectedDTO = BaseDTO.SelectedDTO.builder()
                        .contractStatus(checkContract(행_데이터.get계약시작(), 행_데이터.get계약종료()))
                        .maintenanceStartDate(행_데이터.get계약시작())
                        .maintenanceEndDate(행_데이터.get계약종료())
                        .build();

                BaseDTO baseDTO = BaseDTO.builder()
                        .essential(essentialDTO)
                        .common(commonDTO)
                        .selected(selectedDTO)
                        .build();

                platformProject.updateBaseIssue(이슈키, baseDTO);

                메시지 = "[" + 프로젝트_코드 + "]" + " : " +  프로젝트명 + " - 프로젝트 업데이트를 완료하였습니다.";
                SaveLog.SchedulerResult("updateByExcel\\SUCCESS", 메시지, new Date());
                successCount++;
            }

            if (!프로젝트_정보.isPresent()) {
                메시지 = "[" + 프로젝트_코드 + "]" + " : " +  프로젝트명 + " - 응용정보기술센터의 프로젝트가 아닙니다.";
                SaveLog.SchedulerResult("updateByExcel\\FAIL", 메시지, new Date());
                failCount++;
            }
        }
        SaveLog.SchedulerResult("updateByExcel\\FAIL", "::::: 총 " + failCount + "개 :::::", new Date());
        SaveLog.SchedulerResult("updateByExcel\\SUCCESS", "::::: 총 " + successCount + "개 :::::", new Date());

        return null;
    }
    private List<MaintenanceDTO> readXlsxFile(String fileName, String filePath) {
        List<MaintenanceDTO> dtoList = new ArrayList<>();
        try {
            String file = filePath + "/" + fileName;
            logger.info(":: 유지보수 계약 정보 업로드 파일 명 :: " + file);

            // XLSX 파일 입력 스트림 생성
            FileInputStream fis = new FileInputStream(file);

            // XSSFWorkbook 생성
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fis);

            // 첫 번째 시트 가져오기
            XSSFSheet  sheet = xssfWorkbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                // 헤더 행 건너뛰기
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if(row.getCell(0) == null){
                    break;
                }
                MaintenanceDTO dto = mapExcelDataToDTO(row);
                dtoList.add(dto);
            }
            // 리소스 정리
            fis.close();
            xssfWorkbook.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return dtoList;
    }

    private MaintenanceDTO mapExcelDataToDTO(Row row) {
        MaintenanceDTO dto = new MaintenanceDTO();
        dto.set품의번호(getStringCellValue(row.getCell(0)));
        dto.set회계연도(getStringCellValue(row.getCell(1)));
        dto.set월(getStringCellValue(row.getCell(2)));
        dto.set년월(getStringCellValue(row.getCell(3)));
        dto.set유형(getStringCellValue(row.getCell(4)));
        dto.set발주자(getStringCellValue(row.getCell(5)));
        dto.set계약자(getStringCellValue(row.getCell(6)));
        dto.set프로젝트코드(getStringCellValue(row.getCell(7)));
        dto.set프로젝트명칭(getStringCellValue(row.getCell(8)));
        dto.set영업담당(getStringCellValue(row.getCell(9)));
        dto.set계약시작(formatDate(getStringCellValue(row.getCell(10))));
        dto.set계약종료(formatDate(getStringCellValue(row.getCell(11))));
        return dto;
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null || StringUtils.equals("#VALUE!", cell.toString()) || StringUtils.equals("#N/A", cell.toString())) {
            return ""; // 셀이 없는 경우 빈 문자열 반환
        }
        return cell.toString().trim();
    }

    private String formatDate(String 변환할_날짜) {

        DateTimeFormatter[] 변환_포맷들 = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd-MM월-yyyy").withLocale(Locale.KOREAN),
                DateTimeFormatter.ofPattern("dd-M월-yyyy").withLocale(Locale.KOREAN)
        };

        for (DateTimeFormatter 포맷 : 변환_포맷들) {

            try {
                LocalDate 날짜 = LocalDate.parse(변환할_날짜, 포맷);
                return 날짜.format(목표_포맷);
            } catch(DateTimeParseException e) {}
        }

        logger.warn("[ :: UploadServiceImpl :: ] formatDate -> 날짜 포맷팅 안된 경우 발생");
        return "";
    }

    private String checkContract(String 시작, String 종료) {

        String 계약여부 = "";
        LocalDate 계약시작 = LocalDate.parse(시작, 목표_포맷);
        LocalDate 계약종료 = LocalDate.parse(종료, 목표_포맷);
        LocalDate 오늘 = LocalDate.now();

        if (!오늘.isAfter(계약종료)) {
            계약여부 = "계약";
        } else {
            계약여부 = "미계약";
        }

        return 계약여부;
    }
}
