package com.instello.backend.common.response;

import java.util.List;

public record ApiError(
        String code,
        String message,
        String target,
        List<ApiErrorDetail> details
) {
}
