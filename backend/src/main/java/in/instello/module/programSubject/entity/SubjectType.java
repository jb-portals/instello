package in.instello.module.programSubject.entity;

import in.instello.annotations.GeneratedCuid;
import in.instello.module.institution.entity.Institution;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "subject_types",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_name_and_institution",
          columnNames = {"name", "institution_id"})
    })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubjectType {

  @Id
  @GeneratedCuid
  @Column(length = 24, nullable = false, updatable = false)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "institution_id", nullable = false)
  private Institution institution;

  @OneToMany(mappedBy = "subjectType", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Collection<ProgramSubject> programSubjects;

  @Column(nullable = false, length = 60)
  private String name;

  @Column(nullable = false)
  @Builder.Default
  private boolean builtIn = false;

  @Column(nullable = false)
  @Builder.Default
  private boolean archive = false;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;
}
