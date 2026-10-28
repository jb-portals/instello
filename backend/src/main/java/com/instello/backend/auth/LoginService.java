package com.instello.backend.auth;

import java.time.Duration;
import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.instello.backend.common.exception.RateLimitExceededException;
import com.instello.backend.common.redis.RedisService;
import com.instello.backend.common.security.SessionAuthenticator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class LoginService {

    private static final Duration ATTEMPT_WINDOW = Duration.ofMinutes(15);
    private static final int MAX_ATTEMPTS = 10;

    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticator sessionAuthenticator;
    private final RedisService redisService;

    public LoginService(
            AuthenticationManager authenticationManager,
            SessionAuthenticator sessionAuthenticator,
            RedisService redisService
    ) {
        this.authenticationManager = authenticationManager;
        this.sessionAuthenticator = sessionAuthenticator;
        this.redisService = redisService;
    }

    /**
     * Authenticates with email + password and starts a session. Failures surface as Spring
     * {@link org.springframework.security.core.AuthenticationException}s, mapped by the global handler.
     */
    public void login(String email, String password, HttpServletRequest request, HttpServletResponse response) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String attemptsKey = "login:attempts:" + normalizedEmail + ":" + request.getRemoteAddr();

        if (redisService.increment(attemptsKey, ATTEMPT_WINDOW) > MAX_ATTEMPTS) {
            throw new RateLimitExceededException(
                    "Too many login attempts. Try again later.",
                    Math.max(1, redisService.getRemainingExpiration(attemptsKey))
            );
        }

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(normalizedEmail, password)
        );

        redisService.delete(attemptsKey);
        sessionAuthenticator.authenticate(
                request,
                response,
                authentication.getName(),
                authentication.getAuthorities()
        );
    }
}
