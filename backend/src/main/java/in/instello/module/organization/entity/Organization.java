package in.instello.module.organization.entity;

import in.instello.annotations.GeneratedCuid;
import in.instello.common.address.Address;
import in.instello.module.institution.entity.Institution;
import in.instello.module.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Collection;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "organizations")
public class Organization {

  @Id
  @GeneratedCuid
  @Column(length = 24, nullable = false, updatable = false)
  private String id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String phone;

  @Embedded private Address address;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_id")
  private User createdBy;

  @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
  private Collection<Institution> institutions;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;
}
