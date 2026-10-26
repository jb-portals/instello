package com.instello.backend.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.instello.backend.user.dto.CurrentUserResponse;
import com.instello.backend.user.entity.User;
import com.instello.backend.user.entity.UserPlatformRole;
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User no longer exists"));

        List<UserPlatformRole> roles = user.getRoles() != null ? user.getRoles() : List.of();
        UserProfile profile = user.getUserProfile();

        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getEmailVerifiedAt(),
                roles.stream().map(UserPlatformRole::getRole).toList(),
                profile != null
                        ? new CurrentUserResponse.Profile(profile.getFirstName(), profile.getLastName())
                        : null
        );
    }
}
