package in.instello.common.exception.response;

public final class ErrorCodes {

  public static final String NOT_FOUND = "NOT_FOUND";
  public static final String CONFLICT = "CONFLICT";
  public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
  public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
  public static final String AUTHENTICATION_REQUIRED = "AUTHENTICATION_REQUIRED";
  public static final String FORBIDDEN = "FORBIDDEN";
  public static final String EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED";
  public static final String RATE_LIMITED = "RATE_LIMITED";
  public static final String BAD_REQUEST = "BAD_REQUEST";
  public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

  private ErrorCodes() {}
}
