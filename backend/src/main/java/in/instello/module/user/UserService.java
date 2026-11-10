package in.instello.module.user;

import in.instello.common.exception.app.UnauthorizedException;
import in.instello.module.user.dto.CurrentUserResponse;
import in.instello.module.user.entity.Authority;
import in.instello.module.user.entity.User;
import in.instello.module.user.entity.UserProfile;
import in.instello.module.user.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public CurrentUserResponse getCurrentUser(String email) {
    User user =
        userRepository
            .findByEmailIgnoreCase(email)
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
            : null);
  }
}
