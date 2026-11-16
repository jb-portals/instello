package in.instello.common.exception;

import in.instello.common.exception.app.*;
import in.instello.common.exception.response.ApiProblemDetails;
import in.instello.common.exception.response.ApiProblemError;
import in.instello.common.exception.response.ErrorCodes;
import in.instello.module.auth.otp.InvalidOtpException;
import in.instello.module.auth.otp.OtpRateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler({NoResourceFoundException.class, ResourceNotFoundException.class})
  public ResponseEntity<ApiProblemDetails> handleNoResourceFoundException(
      Exception ex, HttpServletRequest request) {
    return error(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiProblemDetails> handleConflictException(
      ConflictException ex, HttpServletRequest request) {
    return error(HttpStatus.CONFLICT, ErrorCodes.CONFLICT, ex.getMessage(), request);
  }

  @ExceptionHandler({UnauthorizedException.class, InvalidOtpException.class})
  public ResponseEntity<ApiProblemDetails> handleUnauthorizedException(
      RuntimeException ex, HttpServletRequest request) {
    return error(
        HttpStatus.UNAUTHORIZED, ErrorCodes.AUTHENTICATION_REQUIRED, ex.getMessage(), request);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiProblemDetails> handleBadRequestException(
      BadRequestException ex, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST, ex.getMessage(), request);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiProblemDetails> handleForbiddenException(
      ForbiddenException ex, HttpServletRequest request) {
    return error(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN, ex.getMessage(), request);
  }

  // Only raised after the password was proven correct (see SecurityConfiguration), so it is safe to
  // disclose
  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<ApiProblemDetails> handleDisabledException(
      DisabledException ex, HttpServletRequest request) {
    return error(
        HttpStatus.FORBIDDEN,
        ErrorCodes.EMAIL_NOT_VERIFIED,
        "Email address is not verified",
        request);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiProblemDetails> handleBadCredentialsException(
      BadCredentialsException ex, HttpServletRequest request) {
    return error(
        HttpStatus.UNAUTHORIZED,
        ErrorCodes.INVALID_CREDENTIALS,
        "Invalid email or password",
        request);
  }

  @ExceptionHandler({OtpRateLimitException.class, RateLimitExceededException.class})
  public ResponseEntity<ApiProblemDetails> handleRateLimitException(
      RuntimeException ex, HttpServletRequest request) {
    long retryAfterSeconds =
        switch (ex) {
          case OtpRateLimitException rateLimitException ->
              rateLimitException.getRetryAfterSeconds();
          case RateLimitExceededException rateLimitException ->
              rateLimitException.getRetryAfterSeconds();
          default -> throw new IllegalArgumentException("Unsupported rate limit exception", ex);
        };
    return error(
        HttpStatus.TOO_MANY_REQUESTS,
        ErrorCodes.RATE_LIMITED,
        ex.getMessage(),
        request,
        HttpHeaders.RETRY_AFTER,
        String.valueOf(retryAfterSeconds));
  }

  @ExceptionHandler(InvalidApiVersionException.class)
  public ResponseEntity<ApiProblemDetails> handleInvalidApiVersionException(
      InvalidApiVersionException ex, HttpServletRequest request) {
    return error(
        HttpStatus.BAD_REQUEST,
        ErrorCodes.BAD_REQUEST,
        "Unsupported API version: " + ex.getVersion(),
        request);
  }

  @ExceptionHandler(MissingApiVersionException.class)
  public ResponseEntity<ApiProblemDetails> handleMissingApiVersionException(
      MissingApiVersionException ex, HttpServletRequest request) {
    return error(
        HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST, "API version is required", request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiProblemDetails> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<ApiProblemError> details =
        ex.getBindingResult().getFieldErrors().stream().map(this::toDetail).toList();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(
            ApiProblemDetails.of(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCodes.VALIDATION_FAILED,
                "One or more fields are invalid.",
                request.getRequestURI(),
                details));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiProblemDetails> handleResponseStatusException(
      ResponseStatusException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
    if (status == null) {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
    return error(status, codeForStatus(status), message, request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiProblemDetails> handleException(
      Exception ex, HttpServletRequest request) {
    log.error("Unhandled exception", ex);
    return error(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ErrorCodes.INTERNAL_ERROR,
        "An unexpected error occurred",
        request);
  }

  private ApiProblemError toDetail(FieldError fieldError) {
    String code = fieldError.getCode() != null ? fieldError.getCode() : "Invalid";
    String message =
        fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "invalid";
    return new ApiProblemError(code, fieldError.getField(), message);
  }

  private static ResponseEntity<ApiProblemDetails> error(
      HttpStatus status, String code, @Nullable String message, HttpServletRequest request) {
    return ResponseEntity.status(status)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(
            ApiProblemDetails.of(
                status.value(),
                code,
                message != null ? message : status.getReasonPhrase(),
                request.getRequestURI(),
                null));
  }

  private static ResponseEntity<ApiProblemDetails> error(
      HttpStatus status,
      String code,
      @Nullable String message,
      HttpServletRequest request,
      String headerName,
      String headerValue) {
    return ResponseEntity.status(status)
        .header(headerName, headerValue)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(
            ApiProblemDetails.of(
                status.value(),
                code,
                message != null ? message : status.getReasonPhrase(),
                request.getRequestURI(),
                null));
  }

  static String codeForStatus(HttpStatus status) {
    return switch (status) {
      case NOT_FOUND -> ErrorCodes.NOT_FOUND;
      case CONFLICT -> ErrorCodes.CONFLICT;
      case UNAUTHORIZED -> ErrorCodes.AUTHENTICATION_REQUIRED;
      case FORBIDDEN -> ErrorCodes.FORBIDDEN;
      case TOO_MANY_REQUESTS -> ErrorCodes.RATE_LIMITED;
      case BAD_REQUEST -> ErrorCodes.BAD_REQUEST;
      default -> status.is4xxClientError() ? ErrorCodes.BAD_REQUEST : ErrorCodes.INTERNAL_ERROR;
    };
  }
}
