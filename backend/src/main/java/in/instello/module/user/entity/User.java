package in.instello.module.user.entity;

import in.instello.annotations.GeneratedCuid;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;


@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedCuid
    @Column(length = 24, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    // For platform administrators there will be no password
    @Column(columnDefinition = "TEXT")
    private @Nullable String passwordHash;

    @Column(columnDefinition = "TIMESTAMP")
    private @Nullable Instant emailVerifiedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private @Nullable Instant updatedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private @Nullable List<Authority> authorities;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private @Nullable UserProfile userProfile;
}
