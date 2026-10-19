package com.instello.backend.common.exception;

import com.instello.backend.common.response.AppErrorResponse;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<AppErrorResponse> handleNoResourceFoundException(ServletRequest request, ServletResponse response, NoResourceFoundException ex){
        AppErrorResponse appErrorResponse = AppErrorResponse.of(HttpStatus.NOT_FOUND,ex.getMessage());

        return ResponseEntity.status(appErrorResponse.code()).body(appErrorResponse);
    }
}
