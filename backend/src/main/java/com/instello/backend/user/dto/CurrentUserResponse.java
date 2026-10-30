package com.instello.backend.user.dto;

import java.time.Instant;
import java.util.List;

import com.instello.backend.user.entity.AppAuthority;


public record CurrentUserResponse(
        String id,
        String email,
        Instant emailVerifiedAt,
        List<AppAuthority> authorities,
        Profile profile
) {

    public record Profile(
            String firstName,
            String lastName
    ) {
    }
}
