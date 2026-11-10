package in.instello.security;

import in.instello.common.exception.response.ApiErrorResponse;
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

  public void write(HttpServletResponse response, int status, ApiErrorResponse body)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    jsonMapper.writeValue(response.getOutputStream(), body);
  }
}
