package com.saas.cloud_storage_app.common.exception;


import lombok.Getter;

@Getter

public class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    //dung message của ErrorCode
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    //sửa message nếu muốn
    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
