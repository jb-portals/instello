package in.instello.module.organization.repository;

import in.instello.module.organization.entity.OrganizationMember;
import in.instello.module.user.entity.AppAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, String> {

  boolean existsByUser_IdAndOrganization_Id(String userId, String organizationId);

  boolean existsByUser_EmailIgnoreCaseAndOrganization_IdAndRole(
      String email, String organizationId, AppAuthority role);
}
