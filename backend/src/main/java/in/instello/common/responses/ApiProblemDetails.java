package in.instello.common.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.jspecify.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiProblemDetails(
    int status,
    String message,
    String instance,
    ErrorCode code,
    @Nullable List<ApiProblemError> errors) {

  public static ApiProblemDetails of(
      int status,
      ErrorCode code,
      String message,
      String instance,
      @Nullable List<ApiProblemError> errors) {
    return new ApiProblemDetails(status, message, instance, code, errors);
  }
}
