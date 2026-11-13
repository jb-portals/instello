package in.instello.module.circular.repository;

import in.instello.module.circular.entity.Circular;
import in.instello.module.circular.entity.CircularStatus;
import in.instello.module.institution.entity.Institution;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CircularRepository extends JpaRepository<Circular, String> {

  List<Circular> findByInstitution(Institution institution);

  List<Circular> findByStatusAndScheduledAtLessThanEqual(
      CircularStatus status, Instant scheduledAt);

  List<Circular> findByStatusAndExpiresAtLessThanEqual(CircularStatus status, Instant expiresAt);

  Optional<Circular> findByIdAndInstitution(String id, Institution institution);

  @Query(
      """
    SELECT c FROM Circular c
    WHERE c.institution = :institution
    AND (
        :search IS NULL
        OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    AND (
        :status IS NULL
        OR c.status = :status
    )
    """)
    Page<Circular> searchAndFilterCirculars(
        @Param("institution") Institution institution,
        @Param("search") String search,
        @Param("status") CircularStatus status,
        Pageable pageable
    );
}
