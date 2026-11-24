package in.instello.common.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import in.instello.GlobalExceptionHandler;
import in.instello.common.responses.ApiProblemDetails;
import in.instello.common.responses.ApiProblemError;
import in.instello.common.responses.ErrorCode;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void globalErrorsReturnProblemDetailsAndExcludeQueryFromInstance() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v0.1/items/missing");
    request.setQueryString("token=secret");

    ResponseEntity<ApiProblemDetails> response =
        handler.handleConflictException(new ConflictException("Already exists"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    ApiProblemDetails body = Objects.requireNonNull(response.getBody());
    assertThat(body)
        .isEqualTo(
            new ApiProblemDetails(
                409, "Already exists", "/v0.1/items/missing", ErrorCode.CONFLICT, null));
    assertThat(body.instance()).doesNotContain("secret");
  }

  @Test
  void rateLimitKeepsRetryAfterHeader() {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v0.1/auth/otp");
    ResponseEntity<ApiProblemDetails> response =
        handler.handleRateLimitException(
            new in.instello.module.auth.otp.OtpRateLimitException(30), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    assertThat(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("30");
    assertThat(Objects.requireNonNull(response.getBody()).status())
        .isEqualTo(response.getStatusCode().value());
  }

  @Test
  void validationErrorsAreTopLevelProblemExtensions() {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v0.1/invitations");
    BindingResult bindingResult = mock(BindingResult.class);
    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
    when(exception.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors())
        .thenReturn(
            java.util.List.of(
                new FieldError(
                    "invitation",
                    "email",
                    "",
                    false,
                    new String[] {"NotBlank"},
                    null,
                    "must not be blank")));

    ResponseEntity<ApiProblemDetails> response =
        handler.handleMethodArgumentNotValidException(exception, request);

    ApiProblemDetails body = Objects.requireNonNull(response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    assertThat(body.status()).isEqualTo(400);
    assertThat(body.code()).isEqualTo(ErrorCode.VALIDATION_FAILED);
    assertThat(body.errors())
        .containsExactly(new ApiProblemError("NotBlank", "email", "must not be blank"));
  }
}
