package com.utils;

import io.netty.handler.codec.http.multipart.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;


public class FileDownload {
    private static final Logger logger = LoggerFactory.getLogger(FileUpload.class);
    public static void 이미지_다운(String imageUrl,String fileName) throws Exception{
        try {
            logger.info("자라 이미지 링크 다운로드");
            String saveDir = "C:/JIRA/images/"+fileName;
            URL url = new URL(imageUrl);
            InputStream in = url.openStream();
            FileOutputStream out = new FileOutputStream(saveDir);

            StreamUtils.copy(in, out);

            in.close();
            out.close();
        }catch (Exception e){
            logger.error("자라 이미지 링크 다운로드 오류 발생");
            throw new Exception(e.getMessage());
        }


    }
}
