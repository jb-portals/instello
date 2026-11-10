package in.instello.module.institution;

import in.instello.common.exception.app.ResourceNotFoundException;
import in.instello.module.institution.dto.CreateInstitutionRequest;
import in.instello.module.institution.dto.CreateInstitutionResponse;
import in.instello.module.institution.dto.InstitutionResponse;
import in.instello.module.institution.dto.UpdateInstitutionDetailsRequest;
import in.instello.module.institution.entity.Institution;
import in.instello.module.institution.repository.InstitutionRepository;
import in.instello.module.organization.entity.Organization;
import in.instello.module.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

@Service
public class InstitutionService {

  private final InstitutionRepository institutionRepository;
  private final OrganizationRepository organizationRepository;

  public InstitutionService(
      InstitutionRepository institutionRepository, OrganizationRepository organizationRepository) {
    this.institutionRepository = institutionRepository;
    this.organizationRepository = organizationRepository;
  }

  public CreateInstitutionResponse create(
      CreateInstitutionRequest createInstitutionRequest, String organizationId) {

    Organization organization = findOrganization(organizationId);

    Institution newInstitution =
        institutionRepository.save(
            Institution.builder()
                .organization(organization)
                .name(createInstitutionRequest.name())
                .slug(createInstitutionRequest.slug())
                .code(createInstitutionRequest.code())
                .address(createInstitutionRequest.address().toAddress())
                .build());

    return CreateInstitutionResponse.fromInstitution(newInstitution);
  }

  public InstitutionResponse get(String organizationId, String institutionId) {
    Organization organization = findOrganization(organizationId);
    Institution institution =
        institutionRepository
            .findInstitutionByOrganizationAndId(organization, institutionId)
            .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

    return InstitutionResponse.from(institution);
  }

  public InstitutionResponse updateDetails(
      UpdateInstitutionDetailsRequest updateInstitutionDetails,
      String organizationId,
      String institutionId) {
    Organization organization = findOrganization(organizationId);
    Institution institution =
        institutionRepository
            .findInstitutionByOrganizationAndId(organization, institutionId)
            .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

    if (updateInstitutionDetails.name() != null) {
      institution.setName(updateInstitutionDetails.name());
    }

    return InstitutionResponse.from(institution);
  }

  private Organization findOrganization(String organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
  }
}
