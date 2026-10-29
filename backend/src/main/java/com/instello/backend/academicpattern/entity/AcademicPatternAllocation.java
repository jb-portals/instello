package com.instello.backend.academicpattern.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Records that an institution uses a pattern. Institutions are not modeled yet, so {@code institutionId}
 * is a bare uuid and nothing in this slice writes rows. A row makes the pattern structure read-only.
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(
        name = "academic_pattern_allocations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"academic_pattern_id", "institution_id"})
)
public class AcademicPatternAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_pattern_id", nullable = false)
    private AcademicPattern academicPattern;

    @Column(nullable = false)
    private UUID institutionId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
