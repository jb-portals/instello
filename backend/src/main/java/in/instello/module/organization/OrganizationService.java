package in.instello.module.organization;

import in.instello.common.address.Address;
import in.instello.module.academicPattern.AcademicPatternService;
import in.instello.module.organization.entity.Organization;
import in.instello.module.organization.repository.OrganizationRepository;
import in.instello.module.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final AcademicPatternService academicPatternService;

  public OrganizationService(
      OrganizationRepository organizationRepository,
      AcademicPatternService academicPatternService) {
    this.organizationRepository = organizationRepository;
    this.academicPatternService = academicPatternService;
  }

  @Transactional
  public Organization create(
      String name, String email, String phone, Address address, User createdBy) {
    Organization organization =
        Organization.builder()
            .name(name)
            .email(email)
            .phone(phone)
            .address(address)
            .createdBy(createdBy)
            .build();

    Organization saved = organizationRepository.save(organization);
    academicPatternService.seedDefaults(saved);
    return saved;
  }
}
