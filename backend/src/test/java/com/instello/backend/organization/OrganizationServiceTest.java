package com.instello.backend.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import io.github.thibaultmeyer.cuid.CUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.instello.backend.academicPattern.AcademicPatternService;
import com.instello.backend.common.entity.Address;
import com.instello.backend.organization.entity.Organization;
import com.instello.backend.organization.repository.OrganizationRepository;
import com.instello.backend.user.entity.User;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private AcademicPatternService academicPatternService;
    @InjectMocks
    private OrganizationService service;

    @Test
    void createSeedsAcademicPatterns() {
        String organizationId = CUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd").toString();
        when(organizationRepository.save(any())).thenAnswer(invocation -> {
            Organization organization = invocation.getArgument(0);
            organization.setId(organizationId);
            return organization;
        });

        Organization created = service.create(
                "Acme",
                "acme@example.com",
                "1234567890",
                Address.builder().addressLine("1 Main").city("Pune").state("MH").build(),
                User.builder().email("admin@example.com").build()
        );

        assertThat(created.getId()).isEqualTo(organizationId);
        verify(academicPatternService).seedDefaults(created);
    }
}
