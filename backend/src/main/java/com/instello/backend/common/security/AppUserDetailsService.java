package com.instello.backend.common.security;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instello.backend.user.entity.Authority;
import com.instello.backend.user.entity.User;
import com.instello.backend.user.repository.UserRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findWithAuthoritiesByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Accounts without a password (platform admins) sign in with an OTP only
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new UsernameNotFoundException("User not found");
        }

        List<Authority> authorities = user.getAuthorities() != null ? user.getAuthorities() : List.of();

        return new AppUserDetails(
                user.getEmail(),
                user.getPasswordHash(),
                user.getEmailVerifiedAt() != null,
                authorities.stream()
                        .map(authority -> authority.getAuthority().toGrantedAuthority())
                        .toList()
        );
    }
}
