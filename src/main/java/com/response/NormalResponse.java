package com.response;

import com.response.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

public class NormalResponse {

    public static <T> ApiResult<T> success(T response) {
        return new ApiResult<>(true, response, null);
    }

    public static ApiResult<?> error(Throwable throwable, HttpStatus status) {
        return new ApiResult<>(false, null, new ApiError(throwable, status));
    }

    public static ApiResult<?> error(String message, HttpStatus status) {
        return new ApiResult<>(false, null, new ApiError(message, status));
    }

    public static ApiResult<?> error(ErrorCode errorCode, HttpStatus status) {
        return new ApiResult<>(false, null, new ApiError(errorCode, status));
    }

    public static ApiResult<?> error(String message, ErrorCode errorCode, HttpStatus status) {
        return new ApiResult<>(false, null, new ApiError(message, errorCode, status));
    }

    @Getter
    @ToString
    public static class ApiError {
        private String message;
        private String errorCode;
        private int status;

        ApiError(Throwable throwable, HttpStatus status) {
            this(throwable.getMessage(), status);
        }

        ApiError(String message, HttpStatus status) {
            this.message = message;
            this.status = status.value();
        }

        ApiError(ErrorCode errorCode, HttpStatus status) {
            this.message = errorCode.getErrorMessage();
            this.errorCode = errorCode.name();
            this.status = status.value();
        }

        ApiError(String message, ErrorCode errorCode, HttpStatus status) {
            this.message = message;
            this.errorCode = errorCode.name();
            this.status = status.value();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ApiResult<T> {
        private boolean success;
        private T response;
        private ApiError error;
    }
}
