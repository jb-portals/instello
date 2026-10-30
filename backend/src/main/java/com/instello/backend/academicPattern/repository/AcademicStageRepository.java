package com.instello.backend.academicPattern.repository;

import com.instello.backend.academicPattern.entity.AcademicStage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicStageRepository extends JpaRepository<AcademicStage, String> {

    List<AcademicStage> findByAcademicPattern_IdOrderBySequenceNumberAsc(String academicPatternId);

    Optional<AcademicStage> findByIdAndAcademicPattern_Id(String id, String academicPatternId);
}
