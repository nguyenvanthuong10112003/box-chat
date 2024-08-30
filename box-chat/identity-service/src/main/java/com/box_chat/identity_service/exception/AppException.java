package com.box_chat.identity_service.exception;

import lombok.Data;

@Data
public class AppException extends RuntimeException {
    private ErrorCode errorCode;
    public AppException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
