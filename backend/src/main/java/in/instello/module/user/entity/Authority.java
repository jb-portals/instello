package in.instello.module.user.entity;

import in.instello.annotations.GeneratedCuid;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(
    name = "authorities",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "authority"}))
public class Authority {

  @Id
  @GeneratedCuid
  @Column(length = 24, nullable = false, updatable = false)
  private String id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AppAuthority authority;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false, name = "user_id")
  private User user;
}
