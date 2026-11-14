package in.instello.module.circular.entity;

import in.instello.annotations.GeneratedCuid;
import in.instello.module.institution.entity.Institution;
import in.instello.module.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "circulars")
public class Circular {

  @Id
  @GeneratedCuid
  @Column(length = 24, nullable = false, updatable = false)
  private String id;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  @Column(nullable = false)
  private CircularStatus status = CircularStatus.DRAFT;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "institution_id", nullable = false)
  private Institution institution;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "created_by_id", nullable = false)
  private User createdBy;

  private Instant scheduledAt;

  private Instant publishedAt;

  private Instant expiresAt;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;
}
