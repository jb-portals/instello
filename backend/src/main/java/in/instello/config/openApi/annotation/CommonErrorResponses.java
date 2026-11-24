package in.instello.config.openApi.annotation;

import in.instello.common.responses.ApiProblemDetails;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Documents the baseline Problem Details errors returned by API operations. */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(
    value = {
      @ApiResponse(
          responseCode = "400",
          description =
              "Invalid or malformed request. The response code is VALIDATION_FAILED or"
                  + " BAD_REQUEST.",
          content =
              @Content(
                  mediaType = "application/problem+json",
                  schema = @Schema(implementation = ApiProblemDetails.class))),
      @ApiResponse(
          responseCode = "500",
          description = "Unexpected server error (INTERNAL_ERROR).",
          content =
              @Content(
                  mediaType = "application/problem+json",
                  schema = @Schema(implementation = ApiProblemDetails.class)))
    })
public @interface CommonErrorResponses {}
