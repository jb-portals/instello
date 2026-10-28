package com.instello.backend.user.repository;

import com.instello.backend.user.entity.AppAuthority;
import com.instello.backend.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "authorities")
    Optional<User> findWithAuthoritiesByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCaseAndAuthorities_Authority(String email, AppAuthority authority);
}
