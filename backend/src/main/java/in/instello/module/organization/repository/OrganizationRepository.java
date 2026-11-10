package in.instello.module.organization.repository;

import in.instello.module.organization.entity.Organization;

import org.springframework.data.jpa.repository.JpaRepository;



public interface OrganizationRepository extends JpaRepository<Organization, String> {
}
