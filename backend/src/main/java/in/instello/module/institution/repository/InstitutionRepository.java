package in.instello.module.institution.repository;

import in.instello.module.institution.entity.Institution;
import in.instello.module.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution,String> {
    Optional<Institution> findInstitutionByOrganizationAndId(Organization organization, String id);
}
