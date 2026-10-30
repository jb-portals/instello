package com.instello.backend.organization.entity;

import com.instello.backend.common.annotations.GeneratedCuid;
import com.instello.backend.user.entity.AppAuthority;
import com.instello.backend.user.entity.User;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;


@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(
        name = "organization_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "organization_id"})
)
public class OrganizationMember {

    @Id
    @GeneratedCuid
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppAuthority role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
