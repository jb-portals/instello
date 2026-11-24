package in.instello.security;

import in.instello.common.responses.ApiProblemDetails;
import in.instello.common.responses.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

  private final ApiErrorWriter apiErrorWriter;

  public ApiAccessDeniedHandler(ApiErrorWriter apiErrorWriter) {
    this.apiErrorWriter = apiErrorWriter;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {

    ErrorCode code = ErrorCode.FORBIDDEN;
    String message = "Access is denied";

    if (accessDeniedException instanceof CsrfException) {
      boolean tokenProvided = hasCsrfToken(request);
      message = tokenProvided ? "CSRF token is invalid" : "CSRF token is missing";
    }

    apiErrorWriter.write(
        request,
        response,
        HttpStatus.FORBIDDEN.value(),
        ApiProblemDetails.of(
            HttpStatus.FORBIDDEN.value(), code, message, request.getRequestURI(), null));
  }

  private boolean hasCsrfToken(HttpServletRequest request) {
    Object csrfTokenAttribute = request.getAttribute(CsrfToken.class.getName());
    if (csrfTokenAttribute instanceof CsrfToken csrfToken) {
      return StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))
          || StringUtils.hasText(request.getParameter(csrfToken.getParameterName()));
    }

    return StringUtils.hasText(request.getHeader("X-CSRF-TOKEN"))
        || StringUtils.hasText(request.getParameter("_csrf"));
  }
}
