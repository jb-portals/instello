package in.instello.module.program;

import in.instello.annotations.GeneratedCuid;
import in.instello.module.institution.entity.Institution;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "programs",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_program_institution_alias",
          columnNames = {"institution_id", "alias"})
    })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Program {

  @Id
  @GeneratedCuid
  @Column(length = 24, nullable = false, updatable = false)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "institution_id", nullable = false)
  private Institution institution;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String alias;

  @Column(nullable = false)
  @Builder.Default
  private boolean archive = false;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;
}
