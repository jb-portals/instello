package in.instello.module.faculty;

import in.instello.annotations.GeneratedCuid;
import in.instello.common.address.Address;
import in.instello.module.institution.entity.Institution;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "faculty",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_faculty_institution_staffid",
          columnNames = {"institution_id", "staffid"}),
      @UniqueConstraint(
          name = "uk_faculty_institution_email",
          columnNames = {"institution_id", "email"}),
      @UniqueConstraint(
          name = "uk_faculty_institution_primaryPhoneNumber",
          columnNames = {"institution_id", "primaryPhoneNumber"})
    })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Faculty {

  @Id
  @GeneratedCuid
  @Column(nullable = false, updatable = false, length = 24)
  private String id;

  @Column(nullable = false, length = 30)
  private String firstName;

  @Column(length = 30)
  private String lastName;

  @Column(nullable = false)
  private LocalDate dateOfBirth;

  @Column(nullable = false)
  private String email;

  private String profilePhoto;

  @Column(nullable = false, length = 20)
  private String staffId;

  @Column(nullable = false)
  private String designation;

  private String qualification;

  private String specialization;

  private LocalDate joinedDate;

  @Column(nullable = false, length = 15)
  private String primaryPhoneNumber;

  @Column(length = 15)
  private String secondaryPhoneNumber;

  @Embedded private Address address;

  @Column(nullable = false)
  @Builder.Default
  private boolean active = true;

  private Instant inactivatedAt;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, name = "institution_id")
  private Institution institution;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;
}
