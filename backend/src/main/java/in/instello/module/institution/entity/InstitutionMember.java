package in.instello.module.institution.entity;

import in.instello.annotations.GeneratedCuid;
import in.instello.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "institution_members")
public class InstitutionMember {
    @Id
    @GeneratedCuid
    @Column(length = 24, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false)
    @Builder.Default
    private InstitutionRole role = InstitutionRole.STAFF;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
