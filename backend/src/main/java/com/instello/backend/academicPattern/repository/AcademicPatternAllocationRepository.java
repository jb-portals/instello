package com.instello.backend.academicPattern.repository;

import com.instello.backend.academicPattern.entity.AcademicPatternAllocation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;

public interface AcademicPatternAllocationRepository extends JpaRepository<AcademicPatternAllocation, String> {

    boolean existsByAcademicPattern_Id(String academicPatternId);

    @Query("""
            select allocation.academicPattern.id
            from AcademicPatternAllocation allocation
            where allocation.academicPattern.id in :patternIds
            """)
    Set<String> findAllocatedPatternIds(@Param("patternIds") Collection<String> patternIds);
}
