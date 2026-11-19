package in.instello.module.subjectCatalog;

import in.instello.annotations.GeneratedCuid;
import in.instello.module.institution.entity.Institution;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
    name = "subjects",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_program_institution_alias",
          columnNames = {"institution_id", "alias"})
    })
public class Subject {
  @Id
  @GeneratedCuid
  @Column(nullable = false, updatable = false, length = 24)
  private String id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String code;

  @Column(nullable = false, unique = true)
  private String alias;

  @Nullable private String description;

  @Column(nullable = false, unique = true)
  private String colorHash;

  @Column(nullable = false)
  @Builder.Default
  private boolean archive = false;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, name = "institution_id")
  private Institution institution;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;
}
