package com.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveLog {
    private static final String FILE_DIRECTORY = "C:\\log\\"; // 파일이 저장될 디렉토리 경로

    public static void SchedulerResult(String path ,String result, Date date) {
        // 현재 날짜를 가져옴
        Date currentDate = new Date();

        // 파일 이름에 날짜 포함하여 파일 경로 생성
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String fileName = "스케줄러_결과_" + dateFormat.format(currentDate) + ".txt";
        String filePath = FILE_DIRECTORY +path+"\\"+ fileName;

        result = date.toString()+"=================="+result;

        try {
            File file = new File(filePath);

            // 디렉토리가 존재하지 않으면 새로운 디렉토리 생성
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
                System.out.println("새로운 디렉토리가 생성되었습니다. 디렉토리 경로: " + file.getParentFile().getPath());
            }

            // 파일이 존재하지 않으면 새로운 파일 생성
            if (!file.exists()) {
                file.createNewFile();
                System.out.println("새로운 파일이 생성되었습니다. 파일 경로: " + filePath);
            }

            // 파일에 결과 저장
            FileWriter fileWriter = new FileWriter(file, true); // true: 파일 끝에 추가
            fileWriter.write(result);
            fileWriter.write(System.lineSeparator()); // 줄바꿈
            fileWriter.close();

            System.out.println("결과가 파일에 저장되었습니다. 파일 경로: " + filePath);
        } catch (IOException e) {
            System.out.println("파일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
