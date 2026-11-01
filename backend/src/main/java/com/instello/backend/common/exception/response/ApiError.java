package com.instello.backend.common.exception.response;

import java.util.List;

public record ApiError(
        String code,
        String message,
        String target,
        List<ApiErrorDetail> details
) {
}
