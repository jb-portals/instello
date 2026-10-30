package com.instello.backend.academicPattern.repository;

import com.instello.backend.academicPattern.entity.AcademicPattern;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicPatternRepository extends JpaRepository<AcademicPattern, String> {

    List<AcademicPattern> findByOrganization_IdOrderByNameAsc(String organizationId);

    Optional<AcademicPattern> findByIdAndOrganization_Id(String id, String organizationId);
}
