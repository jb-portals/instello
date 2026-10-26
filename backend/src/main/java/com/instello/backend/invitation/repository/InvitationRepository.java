package com.instello.backend.invitation.repository;

import com.instello.backend.invitation.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    List<Invitation> findByAccessRequestIdAndAcceptedAtIsNullAndRevokedAtIsNull(UUID accessRequestId);

    Optional<Invitation> findFirstByAccessRequestIdOrderByCreatedAtDesc(UUID accessRequestId);
}
