package in.instello.module.academicPattern.entity;

import in.instello.annotations.GeneratedCuid;
import in.instello.module.organization.entity.Organization;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Collection;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(
    name = "academic_patterns",
    uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "name"}))
public class AcademicPattern {

  @Id
  @GeneratedCuid
  @Column(length = 24, nullable = false, updatable = false)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 2000)
  private @Nullable String description;

  /** Total course duration in the institution in terms of years. */
  @Column(nullable = false)
  private Integer durationInYears;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AcademicSystemType systemType;

  @OneToMany(mappedBy = "academicPattern", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Collection<AcademicStage> academicStages;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;
}
