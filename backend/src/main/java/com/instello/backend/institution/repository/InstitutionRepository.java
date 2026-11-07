package com.instello.backend.institution.repository;

import com.instello.backend.institution.entity.Institution;
import com.instello.backend.organization.entity.Organization;
import lombok.Builder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution,String> {
    Optional<Institution> findInstitutionByOrganizationAndId(Organization organization, String id);
}
