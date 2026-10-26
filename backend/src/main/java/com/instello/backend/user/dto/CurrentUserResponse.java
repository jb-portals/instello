package com.instello.backend.user.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.instello.backend.user.entity.PlatformRole;

public record CurrentUserResponse(
        UUID id,
        String email,
        Instant emailVerifiedAt,
        List<PlatformRole> roles,
        Profile profile
) {

    public record Profile(
            String firstName,
            String lastName
    ) {
    }
}
