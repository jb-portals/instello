package in.instello.module.programSubject.entity;

import in.instello.annotations.GeneratedCuid;
import in.instello.module.academicPattern.entity.AcademicStage;
import in.instello.module.institution.entity.Institution;
import in.instello.module.program.Program;
import in.instello.module.subjectCatalog.Subject;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

@Entity
@Table(
    name = "program_subjects",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_subject_and_subjectType",
          columnNames = {"subject_id", "subject_type_id"})
    })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgramSubject {

  @Id
  @GeneratedCuid
  @Column(length = 24, nullable = false, updatable = false)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "institution_id", nullable = false, updatable = false)
  private Institution institution;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "program_id", nullable = false)
  private Program program;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "academic_stage_id", nullable = false)
  private AcademicStage academicStage;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "subject_id", nullable = false)
  private Subject subject;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "subject_type_id", nullable = false)
  private SubjectType subjectType;

  @Nullable private String description;

  @Column(nullable = false)
  @Builder.Default
  private boolean archive = false;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;
}
