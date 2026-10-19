package com.instello.backend.common.response;


import org.springframework.http.HttpStatus;

import java.time.Instant;

public record AppErrorResponse(
        int code,
        String status,
        String message,
        Instant timestamp
) {

    public static AppErrorResponse of(HttpStatus code, String message){
        return new AppErrorResponse(
                code.value(),
                code.getReasonPhrase(),
                message,
                Instant.now()
        );
    }
}

