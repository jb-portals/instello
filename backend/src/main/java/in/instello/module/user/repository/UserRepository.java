package in.instello.module.user.repository;

import in.instello.module.user.entity.AppAuthority;
import in.instello.module.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

  Optional<User> findByEmailIgnoreCase(String email);

  @EntityGraph(attributePaths = "authorities")
  Optional<User> findWithAuthoritiesByEmailIgnoreCase(String email);

  Optional<User> findByEmailIgnoreCaseAndAuthorities_Authority(
      String email, AppAuthority authority);
}
