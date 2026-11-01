package com.instello.backend.institution;

import com.instello.backend.common.exception.ResourceNotFoundException;
import com.instello.backend.institution.dto.CreateInstitutionRequest;
import com.instello.backend.institution.dto.CreateInstitutionResponse;
import com.instello.backend.institution.entity.Institution;
import com.instello.backend.institution.repository.InstitutionRepository;
import com.instello.backend.organization.entity.Organization;
import com.instello.backend.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

@Service
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final OrganizationRepository organizationRepository;

    public InstitutionService(InstitutionRepository institutionRepository, OrganizationRepository organizationRepository) {
        this.institutionRepository = institutionRepository;
        this.organizationRepository = organizationRepository;
    }

    public CreateInstitutionResponse create(CreateInstitutionRequest createInstitutionRequest, String organizationId){

        Organization organization = organizationRepository.findById(organizationId).orElseThrow(()-> new ResourceNotFoundException("Organization not found, so can't create institution"));

        Institution newInstitution = institutionRepository.save(
                Institution.builder()
                        .organization(organization)
                        .name(createInstitutionRequest.name())
                        .slug(createInstitutionRequest.slug())
                        .code(createInstitutionRequest.code())
                        .address(createInstitutionRequest.address().toAddress())
                        .build()
        );

        return CreateInstitutionResponse.fromInstitution(newInstitution);
    }

}
