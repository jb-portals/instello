package com.instello.backend.common.exception;

import com.instello.backend.auth.otp.InvalidOtpException;
import com.instello.backend.auth.otp.OtpRateLimitException;
import com.instello.backend.common.response.AppErrorResponse;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<AppErrorResponse> handleInvalidOtpException(InvalidOtpException ex){
        AppErrorResponse appErrorResponse = AppErrorResponse.of(HttpStatus.UNAUTHORIZED, ex.getMessage());

        return ResponseEntity.status(appErrorResponse.code()).body(appErrorResponse);
    }

    @ExceptionHandler(OtpRateLimitException.class)
    public ResponseEntity<AppErrorResponse> handleOtpRateLimitException(OtpRateLimitException ex){
        AppErrorResponse appErrorResponse = AppErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());

        return ResponseEntity.status(appErrorResponse.code())
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
                .body(appErrorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null
                ? fieldError.getField() + " " + fieldError.getDefaultMessage()
                : "Invalid request";
        AppErrorResponse appErrorResponse = AppErrorResponse.of(HttpStatus.BAD_REQUEST, message);

        return ResponseEntity.status(appErrorResponse.code()).body(appErrorResponse);
    }
}
