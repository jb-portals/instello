package in.instello.common.exception.response;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record ApiErrorResponse(ApiError error) {

  public static ApiErrorResponse of(String code, String message) {
    return of(code, message, null, null);
  }

  public static ApiErrorResponse of(String code, String message, @Nullable String target) {
    return of(code, message, target, null);
  }

  public static ApiErrorResponse of(
      String code,
      String message,
      @Nullable String target,
      @Nullable List<ApiErrorDetail> details) {
    return new ApiErrorResponse(new ApiError(code, message, target, details));
  }
}
