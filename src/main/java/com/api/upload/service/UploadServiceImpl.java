package com.api.upload.service;

import com.api.upload.dto.MaintenanceDTO;
import com.jira.issue.service.JiraIssue;
import com.jira.project.model.dao.TB_JML_JpaRepository;
import com.jira.project.model.entity.TB_JML_Entity;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@AllArgsConstructor
@Service("UploadService")
public class UploadServiceImpl implements UploadService {

    @Autowired
    TB_JML_JpaRepository  TB_JML_JpaRepository;

    @Autowired
    JiraIssue jiraIssue;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String uploadMaintenanceDate(String fileName, String filePath) throws Exception {

        // 파일 읽기
        List<MaintenanceDTO> 파일_데이터 = readXlsxFile(fileName,filePath);

        for(MaintenanceDTO 행_데이터 : 파일_데이터){

            TB_JML_Entity 프로젝트_정보 = TB_JML_JpaRepository.findByProjectCode(행_데이터.get프로젝트코드());

            String issueKey  = jiraIssue.getBaseIssueKeyByJiraKey(프로젝트_정보.getKey());




        }



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
        dto.set계약시작(getStringCellValue(row.getCell(10)));
        dto.set계약종료(getStringCellValue(row.getCell(11)));
        return dto;
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return ""; // 셀이 없는 경우 빈 문자열 반환
        }
        return cell.toString();
    }
}
