package com.instello.backend.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            ApiAuthenticationEntryPoint authenticationEntryPoint,
            ApiAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(Customizer.withDefaults())
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/error").permitAll()
                                .requestMatchers("/api-docs/**").permitAll()

                                .requestMatchers(HttpMethod.POST, "/v{version}/auth/login", "/v{version}/auth/admin/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/v{version}/auth/csrf-token").permitAll()

                                // Owner onboarding: preview + accept an invitation, then verify the email with an OTP
                                .requestMatchers(HttpMethod.GET, "/v{version}/organizations/invitations").permitAll()
                                .requestMatchers(HttpMethod.POST, "/v{version}/organizations/invitations/accept").permitAll()
                                .requestMatchers(HttpMethod.POST, "/v{version}/auth/email-verification/**").permitAll()

                                // Anybody can create access request
                                .requestMatchers(HttpMethod.POST, "/v{version}/access-requests").permitAll()

                                .requestMatchers("/v{version}/access-requests/**").hasRole("ADMIN")
                                .anyRequest()
                                .authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session
                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::changeSessionId)
                )
                .build();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        // Runs only after the password matched, so an unverified email is not disclosed to guessers
        provider.setPostAuthenticationChecks(user -> {
            if (user instanceof AppUserDetails details && !details.emailVerified()) {
                throw new DisabledException("Email address is not verified");
            }
        });
        return new ProviderManager(provider);
    }

}
