package com.instello.backend.common.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Login principal for email + password accounts. The account is always "enabled" for Spring's pre-checks so
 * that an unverified email is only reported after the password has been proven correct.
 */
public record AppUserDetails(
        String email,
        String passwordHash,
        boolean emailVerified,
        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
