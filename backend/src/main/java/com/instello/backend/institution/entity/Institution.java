package com.instello.backend.institution.entity;

import com.instello.backend.common.annotations.GeneratedCuid;
import com.instello.backend.common.entity.Address;
import com.instello.backend.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "institutions")
public class Institution {

    @Id
    @GeneratedCuid
    @Column(length = 24, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private @Nullable String logo;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "organization_id")
    private Organization organization;

    @OneToMany(mappedBy = "institution",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Collection<InstitutionMember> institutionMembers;

    @Embedded
    private Address address;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
