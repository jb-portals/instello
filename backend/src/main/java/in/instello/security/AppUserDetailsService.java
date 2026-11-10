package in.instello.security;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.instello.module.user.entity.Authority;
import in.instello.module.user.entity.User;
import in.instello.module.user.repository.UserRepository;

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
