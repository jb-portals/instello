package com.instello.backend.academicPattern.entity;

import com.instello.backend.common.annotations.GeneratedCuid;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(
        name = "academic_stages",
        uniqueConstraints = @UniqueConstraint(columnNames = {"academic_pattern_id", "sequence_number"})
)
public class AcademicStage {

    @Id
    @GeneratedCuid
    @Column(length = 24, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_pattern_id", nullable = false)
    private AcademicPattern academicPattern;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 100)
    private String alias;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private int sequenceNumber;

    @Column(nullable = false)
    private int yearNumber;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
