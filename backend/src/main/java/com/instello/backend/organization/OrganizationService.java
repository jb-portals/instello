package com.instello.backend.organization;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instello.backend.academicpattern.AcademicPatternService;
import com.instello.backend.common.entity.Address;
import com.instello.backend.organization.entity.Organization;
import com.instello.backend.organization.repository.OrganizationRepository;
import com.instello.backend.user.entity.User;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final AcademicPatternService academicPatternService;

    public OrganizationService(
            OrganizationRepository organizationRepository,
            AcademicPatternService academicPatternService
    ) {
        this.organizationRepository = organizationRepository;
        this.academicPatternService = academicPatternService;
    }

    @Transactional
    public Organization create(String name, String email, String phone, Address address, User createdBy) {
        Organization organization = Organization.builder()
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
