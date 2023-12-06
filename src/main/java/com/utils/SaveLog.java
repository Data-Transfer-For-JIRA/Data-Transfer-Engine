package com.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveLog {
        private static final String FILE_DIRECTORY = "C:\\log/"; // 파일이 저장될 디렉토리 경로

        public static void projectSchedulerResult(String result,Date date) {
        // 현재 날짜를 가져옴
        Date currentDate = new Date();

        // 파일 이름에 날짜 포함하여 파일 경로 생성
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String fileName = "프로젝트_스케줄러_결과_" + dateFormat.format(currentDate) + ".txt";
        String filePath = FILE_DIRECTORY + fileName;

        result = date.toString()+" --------------------- "+result;

        try {
            // 파일에 결과 저장
            FileWriter fileWriter = new FileWriter(filePath, true); // true: 파일 끝에 추가
            fileWriter.write(result);
            fileWriter.write(System.lineSeparator()); // 줄바꿈
            fileWriter.close();

            System.out.println("결과가 파일에 저장되었습니다. 파일 경로: " + filePath);
        } catch (IOException e) {
            System.out.println("파일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
