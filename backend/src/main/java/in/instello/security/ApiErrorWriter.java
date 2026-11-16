package in.instello.security;

import in.instello.common.exception.response.ApiProblemDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ApiErrorWriter {

  private final JsonMapper jsonMapper;

  public ApiErrorWriter(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  public void write(
      HttpServletRequest request, HttpServletResponse response, int status, ApiProblemDetails body)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    body =
        new ApiProblemDetails(
            body.type(),
            body.title(),
            body.status(),
            body.detail(),
            request.getRequestURI(),
            body.code(),
            body.errors());
    jsonMapper.writeValue(response.getOutputStream(), body);
  }
}
