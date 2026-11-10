package in.instello.module.institution.entity;

import in.instello.annotations.GeneratedCuid;
import in.instello.common.address.Address;
import in.instello.module.organization.entity.Organization;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Collection;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "institutions")
public class Institution {

  @Id
  @GeneratedCuid
  @Column(length = 24, nullable = false, updatable = false)
  private String id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String slug;

  private @Nullable String logo;

  @Column(nullable = false, unique = true)
  private String code;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, name = "organization_id")
  private Organization organization;

  @OneToMany(mappedBy = "institution", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Collection<InstitutionMember> institutionMembers;

  @Embedded private Address address;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;
}
