package in.instello.common.exception.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.jspecify.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiProblemDetails(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    String code,
    @Nullable List<ApiProblemError> errors) {

  public static ApiProblemDetails of(
      int status,
      String code,
      String detail,
      String instance,
      @Nullable List<ApiProblemError> errors) {
    String slug = code.toLowerCase().replace('_', '-');
    String type = "https://api.instello.in/problems/" + slug;
    String title = titleFor(code, status);
    return new ApiProblemDetails(type, title, status, detail, instance, code, errors);
  }

  private static String titleFor(String code, int status) {
    return switch (code) {
      case ErrorCodes.VALIDATION_FAILED -> "Request validation failed";
      case ErrorCodes.NOT_FOUND -> "Resource not found";
      case ErrorCodes.CONFLICT -> "Request conflicts with the current resource state";
      case ErrorCodes.INVALID_CREDENTIALS -> "Invalid credentials";
      case ErrorCodes.INTERNAL_ERROR -> "Internal Server Error";
      case ErrorCodes.AUTHENTICATION_REQUIRED -> "Authentication is required";
      case ErrorCodes.FORBIDDEN -> "Access is denied";
      case ErrorCodes.EMAIL_NOT_VERIFIED -> "Email address is not verified";
      case ErrorCodes.RATE_LIMITED -> "Request limit exceeded";
      case ErrorCodes.BAD_REQUEST -> "Bad request";
      default -> status >= 500 ? "Internal Server Error" : "Request failed";
    };
  }
}
