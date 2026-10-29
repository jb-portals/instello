package com.instello.backend.academicpattern.repository;

import com.instello.backend.academicpattern.entity.AcademicStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcademicStageRepository extends JpaRepository<AcademicStage, UUID> {

    List<AcademicStage> findByAcademicPattern_IdOrderBySequenceNumberAsc(UUID academicPatternId);

    Optional<AcademicStage> findByIdAndAcademicPattern_Id(UUID id, UUID academicPatternId);
}
