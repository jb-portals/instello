package in.instello.module.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import in.instello.common.address.Address;
import in.instello.module.academicPattern.AcademicPatternService;
import in.instello.module.organization.entity.Organization;
import in.instello.module.organization.repository.OrganizationRepository;
import in.instello.module.user.entity.User;
import io.github.thibaultmeyer.cuid.CUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock private OrganizationRepository organizationRepository;
  @Mock private AcademicPatternService academicPatternService;
  @InjectMocks private OrganizationService service;

  @Test
  void createSeedsAcademicPatterns() {
    String organizationId = CUID.fromString("d12345678901234567890123").toString();
    when(organizationRepository.save(any()))
        .thenAnswer(
            invocation -> {
              Organization organization = invocation.getArgument(0);
              organization.setId(organizationId);
              return organization;
            });

    Organization created =
        service.create(
            "Acme",
            "acme@example.com",
            "1234567890",
            Address.builder()
                .lineOne("1 Main")
                .lineTwo("2nd Main")
                .city("Pune")
                .state("MH")
                .postalCode("563106")
                .build(),
            User.builder().email("admin@example.com").build());

    assertThat(created.getId()).isEqualTo(organizationId);
    verify(academicPatternService).seedDefaults(created);
  }
}
