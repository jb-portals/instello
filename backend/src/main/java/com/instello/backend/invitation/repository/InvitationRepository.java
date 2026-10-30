package com.instello.backend.invitation.repository;

import com.instello.backend.invitation.entity.Invitation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface InvitationRepository extends JpaRepository<Invitation, String> {

    List<Invitation> findByAccessRequestIdAndAcceptedAtIsNullAndRevokedAtIsNull(String accessRequestId);

    Optional<Invitation> findFirstByAccessRequestIdOrderByCreatedAtDesc(String accessRequestId);

    Optional<Invitation> findByTokenHash(String tokenHash);

    Optional<Invitation> findFirstByEmailAndAcceptedAtIsNullAndRevokedAtIsNullOrderByCreatedAtDesc(String email);

    boolean existsByEmailAndAcceptedAtIsNullAndRevokedAtIsNull(String email);

    List<Invitation> findByAcceptedAtIsNotNull();
}
