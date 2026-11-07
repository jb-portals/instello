package com.instello.backend.common.exception.response;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record ApiError(
        String code,
        String message,
        @Nullable String target,
        @Nullable List<ApiErrorDetail> details
) {
}
