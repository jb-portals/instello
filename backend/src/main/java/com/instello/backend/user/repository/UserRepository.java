package com.instello.backend.user.repository;

import com.instello.backend.user.entity.PlatformRole;
import com.instello.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCaseAndRoles_Role(String email, PlatformRole role);
}
