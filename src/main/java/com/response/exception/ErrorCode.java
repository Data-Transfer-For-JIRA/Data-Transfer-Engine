package com.response.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    DATA_FORMAT_ERROR("데이터 포맷이 올바르지 않습니다.");

    private final String errorMessage;

    public String getErrorMessage(Object... arg) {
        return String.format(errorMessage, arg);
    }
}
