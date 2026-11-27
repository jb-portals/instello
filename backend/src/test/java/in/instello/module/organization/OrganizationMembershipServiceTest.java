package in.instello.module.organization;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import in.instello.common.exceptions.ForbiddenException;
import in.instello.module.invitation.repository.InvitationRepository;
import in.instello.module.organization.repository.OrganizationMemberRepository;
import in.instello.module.organization.repository.OrganizationRepository;
import in.instello.module.user.entity.AppAuthority;
import in.instello.module.user.entity.User;
import in.instello.module.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationMembershipServiceTest {

  private static final String ORGANIZATION_ID = "organization";
  private static final String EMAIL = "member@example.com";

  @Mock private OrganizationRepository organizations;
  @Mock private OrganizationMemberRepository members;
  @Mock private UserRepository users;
  @Mock private InvitationRepository invitations;
  @InjectMocks private OrganizationMembershipService service;

  @BeforeEach
  void setUpMember() {
    when(organizations.existsById(ORGANIZATION_ID)).thenReturn(true);
    when(users.findByEmailIgnoreCase(EMAIL))
        .thenReturn(Optional.of(User.builder().id("user").email(EMAIL).build()));
    when(members.existsByUser_IdAndOrganization_Id("user", ORGANIZATION_ID)).thenReturn(true);
  }

  @Test
  void membershipAloneAllowsReadsButDoesNotAllowOwnerOperations() {
    assertThatCode(() -> service.requireMember(ORGANIZATION_ID, EMAIL)).doesNotThrowAnyException();
    assertThatThrownBy(() -> service.requireOwner(ORGANIZATION_ID, EMAIL))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  void ownerMembershipAllowsOwnerOperations() {
    when(members.existsByUser_EmailIgnoreCaseAndOrganization_IdAndRole(
            EMAIL, ORGANIZATION_ID, AppAuthority.ROLE_OWNER))
        .thenReturn(true);

    assertThatCode(() -> service.requireOwner(ORGANIZATION_ID, EMAIL)).doesNotThrowAnyException();
  }
}
