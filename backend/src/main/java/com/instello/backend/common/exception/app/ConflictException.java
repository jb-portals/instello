package com.instello.backend.common.exception.app;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
