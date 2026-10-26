package com.instello.backend.invitation.entity;

import com.instello.backend.accessRequest.entity.AccessRequest;
import com.instello.backend.organization.entity.Organization;
import com.instello.backend.user.entity.PlatformRole;
import com.instello.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "invitations")
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlatformRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_request_id")
    private AccessRequest accessRequest;

    // SHA-256 of the emailed token; the raw token is never persisted
    @Column(nullable = false, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant acceptedAt;

    private Instant revokedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id")
    private User invitedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public InvitationStatus getStatus() {
        if (acceptedAt != null) {
            return InvitationStatus.ACCEPTED;
        }
        if (revokedAt != null) {
            return InvitationStatus.REVOKED;
        }
        if (expiresAt.isBefore(Instant.now())) {
            return InvitationStatus.EXPIRED;
        }
        return InvitationStatus.PENDING;
    }
}
