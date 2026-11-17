package in.instello.module.program;

import in.instello.annotations.GeneratedCuid;
import in.instello.module.institution.entity.Institution;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

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
  private boolean archived = false;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}
