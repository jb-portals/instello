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
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.accept.MissingApiVersionException;
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

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<AppErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex){
        AppErrorResponse appErrorResponse = AppErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage());

        return ResponseEntity.status(appErrorResponse.code()).body(appErrorResponse);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<AppErrorResponse> handleConflictException(ConflictException ex){
        AppErrorResponse appErrorResponse = AppErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage());

        return ResponseEntity.status(appErrorResponse.code()).body(appErrorResponse);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<AppErrorResponse> handleRateLimitExceededException(RateLimitExceededException ex){
        AppErrorResponse appErrorResponse = AppErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());

        return ResponseEntity.status(appErrorResponse.code())
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
                .body(appErrorResponse);
    }

    @ExceptionHandler(InvalidApiVersionException.class)
    public ResponseEntity<AppErrorResponse> handleInvalidApiVersionException(InvalidApiVersionException ex){
        AppErrorResponse appErrorResponse = AppErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "Unsupported API version: " + ex.getVersion()
        );

        return ResponseEntity.status(appErrorResponse.code()).body(appErrorResponse);
    }

    @ExceptionHandler(MissingApiVersionException.class)
    public ResponseEntity<AppErrorResponse> handleMissingApiVersionException(MissingApiVersionException ex){
        AppErrorResponse appErrorResponse = AppErrorResponse.of(HttpStatus.BAD_REQUEST, "API version is required");

        return ResponseEntity.status(appErrorResponse.code()).body(appErrorResponse);
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
