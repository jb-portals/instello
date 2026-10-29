package com.instello.backend.organization.repository;

import com.instello.backend.organization.entity.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

    boolean existsByUser_IdAndOrganization_Id(UUID userId, UUID organizationId);
}
