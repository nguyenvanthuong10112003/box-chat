package com.box_chat.identity_service.exception;

import com.box_chat.identity_service.dto.response.ResponseAPI;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class CustomGlobalException {
    @ExceptionHandler(AppException.class)
    ResponseEntity<ResponseAPI<?>> appException(AppException appException) {
        return ResponseEntity
            .status(appException.getErrorCode().getHttpStatus())
            .body(ResponseAPI.builder()
                .statusCode(appException.getErrorCode().getCode())
                .message(appException.getErrorCode().getMessage())
                .build()
            );
    }

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<ResponseAPI<?>> runtimeException(RuntimeException runtimeException) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ResponseAPI.builder()
                .statusCode(-1)
                .message(runtimeException.getMessage())
                .build()
            );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ResponseAPI<?>> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ResponseAPI.builder()
                .statusCode(-1)
                .message(e.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(";")))
                .build()
            );
    }
}
