package com.instello.backend.accessRequest.entity;

public enum AccessRequestStatus {
    PENDING,
    CONTACTED,
    APPROVED,
    REJECTED;

    public boolean isOpen() {
        return this == PENDING || this == CONTACTED;
    }
}
