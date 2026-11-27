package in.instello.module.institution.repository;

import in.instello.module.institution.entity.Institution;
import in.instello.module.organization.entity.Organization;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InstitutionRepository extends JpaRepository<Institution, String> {
  Optional<Institution> findInstitutionByOrganizationAndId(Organization organization, String id);

  Optional<Institution> findByIdOrSlug(String id, String slug);

  boolean existsByAcademicPattern_Id(String patternId);

  @Query(
      """
      select distinct institution.academicPattern.id from Institution institution
      where institution.academicPattern.id in :patternIds
      """)
  Set<String> findReferencedAcademicPatternIds(@Param("patternIds") Collection<String> patternIds);
}
