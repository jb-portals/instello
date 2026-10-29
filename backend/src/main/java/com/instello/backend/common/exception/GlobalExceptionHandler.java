package com.instello.backend.common.exception;

import com.instello.backend.auth.otp.InvalidOtpException;
import com.instello.backend.auth.otp.OtpRateLimitException;
import com.instello.backend.common.response.ApiErrorDetail;
import com.instello.backend.common.response.ApiErrorResponse;
import com.instello.backend.common.response.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.accept.MissingApiVersionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflictException(ConflictException ex) {
        return error(HttpStatus.CONFLICT, ErrorCodes.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(BadRequestException ex) {
        return error(HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        return error(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbiddenException(ForbiddenException ex) {
        return error(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN, ex.getMessage());
    }

    // Only raised after the password was proven correct (see SecurityConfiguration), so it is safe to disclose
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleDisabledException(DisabledException ex) {
        return error(HttpStatus.FORBIDDEN, ErrorCodes.EMAIL_NOT_VERIFIED, "Email address is not verified");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Invalid email or password");
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidOtpException(InvalidOtpException ex) {
        return error(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(OtpRateLimitException.class)
    public ResponseEntity<ApiErrorResponse> handleOtpRateLimitException(OtpRateLimitException ex) {
        return error(
                HttpStatus.TOO_MANY_REQUESTS,
                ErrorCodes.RATE_LIMITED,
                ex.getMessage(),
                HttpHeaders.RETRY_AFTER,
                String.valueOf(ex.getRetryAfterSeconds())
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceededException(RateLimitExceededException ex) {
        return error(
                HttpStatus.TOO_MANY_REQUESTS,
                ErrorCodes.RATE_LIMITED,
                ex.getMessage(),
                HttpHeaders.RETRY_AFTER,
                String.valueOf(ex.getRetryAfterSeconds())
        );
    }

    @ExceptionHandler(InvalidApiVersionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidApiVersionException(InvalidApiVersionException ex) {
        return error(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.BAD_REQUEST,
                "Unsupported API version: " + ex.getVersion()
        );
    }

    @ExceptionHandler(MissingApiVersionException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingApiVersionException(MissingApiVersionException ex) {
        return error(HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST, "API version is required");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        List<ApiErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse.of(
                ErrorCodes.VALIDATION_FAILED,
                "One or more fields are invalid",
                null,
                details
        ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return error(status, codeForStatus(status), message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR, "An unexpected error occurred");
    }

    private ApiErrorDetail toDetail(FieldError fieldError) {
        String code = fieldError.getCode() != null ? fieldError.getCode() : "Invalid";
        String message = fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "invalid";
        return new ApiErrorDetail(code, fieldError.getField(), message);
    }

    private static ResponseEntity<ApiErrorResponse> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(ApiErrorResponse.of(code, message));
    }

    private static ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            String code,
            String message,
            String headerName,
            String headerValue
    ) {
        return ResponseEntity.status(status)
                .header(headerName, headerValue)
                .body(ApiErrorResponse.of(code, message));
    }

    static String codeForStatus(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> ErrorCodes.NOT_FOUND;
            case CONFLICT -> ErrorCodes.CONFLICT;
            case UNAUTHORIZED -> ErrorCodes.UNAUTHORIZED;
            case FORBIDDEN -> ErrorCodes.FORBIDDEN;
            case TOO_MANY_REQUESTS -> ErrorCodes.RATE_LIMITED;
            case BAD_REQUEST -> ErrorCodes.BAD_REQUEST;
            default -> status.is4xxClientError() ? ErrorCodes.BAD_REQUEST : ErrorCodes.INTERNAL_ERROR;
        };
    }
}
