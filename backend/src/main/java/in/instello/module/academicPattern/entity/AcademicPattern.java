package in.instello.module.academicPattern.entity;

import in.instello.annotations.GeneratedCuid;
import in.instello.module.organization.entity.Organization;
import jakarta.persistence.*;
import java.time.Instant;
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
    uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "template_key"}))
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

  @Column(nullable = false)
  private int durationInYears;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AcademicSystemType systemType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AcademicTemplateKey templateKey;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;
}
