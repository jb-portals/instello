package in.instello.common.exceptions;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException
    implements TooManyRequestsException {

  private final long retryAfterSeconds;

  public RateLimitExceededException(String message, long retryAfterSeconds) {
    super(message);
    this.retryAfterSeconds = retryAfterSeconds;
  }
}
