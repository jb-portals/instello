package in.instello.security;

import in.instello.common.exception.response.ApiProblemDetails;
import in.instello.common.exception.response.ErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ApiErrorWriter apiErrorWriter;

  public ApiAuthenticationEntryPoint(ApiErrorWriter apiErrorWriter) {
    this.apiErrorWriter = apiErrorWriter;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    apiErrorWriter.write(
        request,
        response,
        HttpStatus.UNAUTHORIZED.value(),
        ApiProblemDetails.of(
            HttpStatus.UNAUTHORIZED.value(),
            ErrorCodes.AUTHENTICATION_REQUIRED,
            "Authentication is required",
            request.getRequestURI(),
            null));
  }
}
