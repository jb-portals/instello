package com.instello.backend.user;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instello.backend.common.exception.UnauthorizedException;
import com.instello.backend.user.dto.CurrentUserResponse;
import com.instello.backend.user.entity.Authority;
import com.instello.backend.user.entity.User;
import com.instello.backend.user.entity.UserProfile;
import com.instello.backend.user.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("User no longer exists"));

        List<Authority> authorities = user.getAuthorities() != null ? user.getAuthorities() : List.of();
        UserProfile profile = user.getUserProfile();

        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getEmailVerifiedAt(),
                authorities.stream().map(Authority::getAuthority).toList(),
                profile != null
                        ? new CurrentUserResponse.Profile(profile.getFirstName(), profile.getLastName())
                        : null
        );
    }
}
