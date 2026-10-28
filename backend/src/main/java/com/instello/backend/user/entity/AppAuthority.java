package com.instello.backend.user.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Authorities stored in the {@code authorities} table. The {@code ROLE_} prefix keeps
 * {@code hasRole("ADMIN")} / {@code hasRole("OWNER")} working in Spring Security.
 */
public enum AppAuthority {
    ROLE_ADMIN,
    ROLE_OWNER,
    ROLE_USER;

    public GrantedAuthority toGrantedAuthority() {
        return new SimpleGrantedAuthority(name());
    }
}
