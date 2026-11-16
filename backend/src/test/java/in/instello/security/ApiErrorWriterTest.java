package in.instello.security;

import static org.assertj.core.api.Assertions.assertThat;

import in.instello.common.exception.response.ApiProblemDetails;
import in.instello.common.exception.response.ErrorCodes;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

class ApiErrorWriterTest {

  @Test
  void securityErrorsUseProblemJsonWithRequestInstance() throws Exception {
    ApiErrorWriter writer = new ApiErrorWriter(JsonMapper.builder().build());
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v0.1/private");
    request.setQueryString("access_token=secret");
    MockHttpServletResponse response = new MockHttpServletResponse();

    writer.write(
        request,
        response,
        HttpStatus.UNAUTHORIZED.value(),
        ApiProblemDetails.of(
            HttpStatus.UNAUTHORIZED.value(),
            ErrorCodes.AUTHENTICATION_REQUIRED,
            "Authentication is required",
            "/v0.1/private",
            null));

    assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(response.getContentType()).isEqualTo("application/problem+json");
    assertThat(response.getContentAsString())
        .contains(
            "\"status\":401",
            "\"code\":\"AUTHENTICATION_REQUIRED\"",
            "\"instance\":\"/v0.1/private\"")
        .doesNotContain("access_token", "errors");
  }

  @Test
  void accessDeniedUsesProblemJson() throws Exception {
    ApiErrorWriter writer = new ApiErrorWriter(JsonMapper.builder().build());
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v0.1/admin");
    MockHttpServletResponse response = new MockHttpServletResponse();

    writer.write(
        request,
        response,
        HttpStatus.FORBIDDEN.value(),
        ApiProblemDetails.of(
            HttpStatus.FORBIDDEN.value(),
            ErrorCodes.FORBIDDEN,
            "Access is denied",
            "/v0.1/admin",
            null));

    assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(response.getContentType()).isEqualTo("application/problem+json");
    assertThat(response.getContentAsString()).contains("\"status\":403", "\"code\":\"FORBIDDEN\"");
  }
}
