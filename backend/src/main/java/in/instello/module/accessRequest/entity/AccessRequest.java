package in.instello.module.accessRequest.entity;

import in.instello.annotations.GeneratedCuid;
import in.instello.common.address.Address;
import in.instello.module.organization.entity.Organization;
import in.instello.module.user.entity.User;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import org.jspecify.annotations.Nullable;


@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(
        name = "access_requests",
        indexes = {
                @Index(name = "idx_access_requests_email_status", columnList = "email, status"),
                @Index(name = "idx_access_requests_status", columnList = "status")
        }
)
public class AccessRequest {

    @Id
    @GeneratedCuid
    @Column(length = 24, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String organizationName;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Embedded
    private @Nullable Address address;

    @Column(columnDefinition = "TEXT")
    private @Nullable String message;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private AccessRequestStatus status = AccessRequestStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private @Nullable String adminNotes;

    @Column(columnDefinition = "TEXT")
    private @Nullable String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private @Nullable User reviewedBy;

    private @Nullable Instant reviewedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", unique = true)
    private @Nullable Organization organization;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
