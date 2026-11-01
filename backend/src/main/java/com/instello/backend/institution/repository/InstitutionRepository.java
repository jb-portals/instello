package com.instello.backend.institution.repository;

import com.instello.backend.institution.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionRepository extends JpaRepository<Institution,String> {}
