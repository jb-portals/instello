package in.instello.module.academicPattern.entity;

import in.instello.annotations.GeneratedCuid;
import in.instello.module.institution.entity.Institution;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Records that an institution uses a pattern. Institutions are not modeled yet, so {@code
 * institutionId} is a bare cuid and nothing in this slice writes rows. A row makes the pattern
 * structure read-only.
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(
    name = "academic_pattern_allocations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"academic_pattern_id", "institution_id"}))
public class AcademicPatternAllocation {

  @Id
  @GeneratedCuid
  @Column(length = 24, nullable = false, updatable = false)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "academic_pattern_id", nullable = false)
  private AcademicPattern academicPattern;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "institution_id", nullable = false, updatable = false)
  private Institution institution;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;
}
