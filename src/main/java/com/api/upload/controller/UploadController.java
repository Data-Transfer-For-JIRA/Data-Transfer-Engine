package com.api.upload.controller;


import com.api.upload.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/upload/file")
public class UploadController {

    @Autowired
    UploadService uploadService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*
    *  파일로 유지보수 계약 갱신 데이터 업로드
    * */
    @ResponseBody
    @RequestMapping(
            value = {"/maintenance/date"},
            method = {RequestMethod.POST}
    )
    public String 유지보수_계약기간_파일_업로드(@RequestParam String fileName, @RequestParam String filePath) throws Exception {

        logger.info(":: 유지보수 계약 정보 업로드 컨트롤러 ::");

        return uploadService.uploadMaintenanceDate(fileName, filePath);
    }
}
