package com.instello.backend.organization.repository;

import com.instello.backend.organization.entity.OrganizationMember;

import org.springframework.data.jpa.repository.JpaRepository;



public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, String> {

    boolean existsByUser_IdAndOrganization_Id(String userId, String organizationId);
}
