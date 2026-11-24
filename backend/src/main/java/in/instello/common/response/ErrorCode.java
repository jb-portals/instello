package in.instello.common.response;

/** Stable machine-readable codes returned in API problem responses. */
public enum ErrorCode {
  NOT_FOUND,
  CONFLICT,
  VALIDATION_FAILED,
  INVALID_CREDENTIALS,
  AUTHENTICATION_REQUIRED,
  FORBIDDEN,
  EMAIL_NOT_VERIFIED,
  RATE_LIMITED,
  BAD_REQUEST,
  INTERNAL_ERROR
}
