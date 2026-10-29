package com.instello.backend.academicpattern.repository;

import com.instello.backend.academicpattern.entity.AcademicPattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcademicPatternRepository extends JpaRepository<AcademicPattern, UUID> {

    List<AcademicPattern> findByOrganization_IdOrderByNameAsc(UUID organizationId);

    Optional<AcademicPattern> findByIdAndOrganization_Id(UUID id, UUID organizationId);
}
