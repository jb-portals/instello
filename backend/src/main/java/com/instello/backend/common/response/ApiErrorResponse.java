package com.instello.backend.common.response;

import java.util.List;

public record ApiErrorResponse(ApiError error) {

    public static ApiErrorResponse of(String code, String message) {
        return of(code, message, null, null);
    }

    public static ApiErrorResponse of(String code, String message, String target) {
        return of(code, message, target, null);
    }

    public static ApiErrorResponse of(
            String code,
            String message,
            String target,
            List<ApiErrorDetail> details
    ) {
        return new ApiErrorResponse(new ApiError(code, message, target, details));
    }
}
