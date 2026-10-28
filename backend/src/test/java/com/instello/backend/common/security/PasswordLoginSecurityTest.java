package com.instello.backend.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.instello.backend.user.entity.AppAuthority;
import com.instello.backend.user.entity.Authority;
import com.instello.backend.user.entity.User;
import com.instello.backend.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PasswordLoginSecurityTest {

    private static final String EMAIL = "owner@example.com";

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AppUserDetailsService userDetailsService;
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        userDetailsService = new AppUserDetailsService(userRepository);
        authenticationManager = new SecurityConfiguration().authenticationManager(userDetailsService, passwordEncoder);
    }

    private User user(String passwordHash, Instant verifiedAt, AppAuthority... authorities) {
        User user = User.builder()
                .email(EMAIL)
                .passwordHash(passwordHash)
                .emailVerifiedAt(verifiedAt)
                .authorities(new ArrayList<>())
                .build();
        for (AppAuthority authority : authorities) {
            user.getAuthorities().add(Authority.builder().authority(authority).user(user).build());
        }
        return user;
    }

    private Authentication attempt(String password) {
        return authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(EMAIL, password));
    }

    @Test
    void verifiedOwnerAuthenticatesWithOwnerAuthority() {
        when(userRepository.findWithAuthoritiesByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(
                user(passwordEncoder.encode("s3cret-password"), Instant.now(), AppAuthority.ROLE_OWNER)));

        Authentication authentication = attempt("s3cret-password");

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getAuthorities()).extracting("authority").contains("ROLE_OWNER");
    }

    @Test
    void adminWithoutPasswordCannotUsePasswordLogin() {
        when(userRepository.findWithAuthoritiesByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(user(null, Instant.now(), AppAuthority.ROLE_ADMIN)));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(EMAIL))
                .isInstanceOf(UsernameNotFoundException.class);
        assertThatThrownBy(() -> attempt("anything")).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void wrongPasswordIsBadCredentials() {
        when(userRepository.findWithAuthoritiesByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(
                user(passwordEncoder.encode("s3cret-password"), Instant.now(), AppAuthority.ROLE_OWNER)));

        assertThatThrownBy(() -> attempt("wrong")).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void unverifiedEmailIsBlockedOnlyAfterCorrectPassword() {
        when(userRepository.findWithAuthoritiesByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(user(passwordEncoder.encode("s3cret-password"), null)));

        assertThatThrownBy(() -> attempt("wrong")).isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> attempt("s3cret-password")).isInstanceOf(DisabledException.class);
    }

    @Test
    void unknownUserIsBadCredentials() {
        when(userRepository.findWithAuthoritiesByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attempt("whatever")).isInstanceOf(BadCredentialsException.class);
    }
}
