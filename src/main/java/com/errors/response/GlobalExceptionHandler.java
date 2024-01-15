package com.errors.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    public ResponseEntity<?> httpMessageNotReadableException(HttpMessageNotReadableException e){

        logger.warn("데이터 포맷 오류 발생");

        return null;
    }

}
