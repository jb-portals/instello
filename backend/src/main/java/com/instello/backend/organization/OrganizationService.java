package com.instello.backend.organization;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instello.backend.common.entity.Address;
import com.instello.backend.organization.entity.Organization;
import com.instello.backend.organization.repository.OrganizationRepository;
import com.instello.backend.user.entity.User;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
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

        return organizationRepository.save(organization);
    }
}
