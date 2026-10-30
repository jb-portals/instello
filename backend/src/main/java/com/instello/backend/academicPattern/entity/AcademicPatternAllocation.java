package com.instello.backend.academicPattern.entity;

import com.instello.backend.common.annotations.GeneratedCuid;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Records that an institution uses a pattern. Institutions are not modeled yet, so {@code institutionId}
 * is a bare cuid and nothing in this slice writes rows. A row makes the pattern structure read-only.
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
    @GeneratedCuid
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_pattern_id", nullable = false)
    private AcademicPattern academicPattern;

    @Column(nullable = false)
    private String institutionId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
