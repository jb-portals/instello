package com.instello.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.instello.backend.auth.emailverification.EmailVerificationService;
import com.instello.backend.auth.otp.InvalidOtpException;
import com.instello.backend.auth.otp.OtpRateLimitException;
import com.instello.backend.auth.otp.OtpService;
import com.instello.backend.common.exception.ConflictException;
import com.instello.backend.invitation.entity.Invitation;
import com.instello.backend.invitation.repository.InvitationRepository;
import com.instello.backend.organization.OrganizationMembershipService;
import com.instello.backend.organization.entity.Organization;
import com.instello.backend.user.entity.AppAuthority;
import com.instello.backend.user.entity.User;
import com.instello.backend.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    private static final String EMAIL = "owner@example.com";

    @Mock
    private OtpService otpService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private OrganizationMembershipService membershipService;
    @InjectMocks
    private EmailVerificationService service;

    private User unverifiedUser() {
        return User.builder().email(EMAIL).authorities(new ArrayList<>()).build();
    }

    private Invitation openInvitation() {
        return Invitation.builder().email(EMAIL).role(AppAuthority.ROLE_OWNER).build();
    }

    @Test
    void validCodeVerifiesEmailAcceptsInvitationAndGrantsOwnerAuthority() {
        User user = unverifiedUser();
        Invitation invitation = openInvitation();
        Organization organization = Organization.builder()
                .name("Acme")
                .email("acme@example.com")
                .phone("1234567890")
                .build();
        organization.setId(UUID.randomUUID());
        invitation.setOrganization(organization);
        when(userRepository.findWithAuthoritiesByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));
        when(invitationRepository.findFirstByEmailAndAcceptedAtIsNullAndRevokedAtIsNullOrderByCreatedAtDesc(EMAIL))
                .thenReturn(Optional.of(invitation));

        service.verify(" Owner@Example.com ", "123456");

        Mockito.verify(otpService).verifyEmailVerificationOtp(EMAIL, "123456");
        assertThat(user.getEmailVerifiedAt()).isNotNull();
        assertThat(invitation.getAcceptedAt()).isNotNull();
        assertThat(user.getAuthorities()).hasSize(1);
        assertThat(user.getAuthorities().getFirst().getAuthority()).isEqualTo(AppAuthority.ROLE_OWNER);
        Mockito.verify(membershipService).ensureMember(user, organization, AppAuthority.ROLE_OWNER);
    }

    @Test
    void wrongCodeLeavesUserUntouched() {
        doThrow(new InvalidOtpException()).when(otpService).verifyEmailVerificationOtp(EMAIL, "000000");

        assertThatThrownBy(() -> service.verify(EMAIL, "000000")).isInstanceOf(InvalidOtpException.class);
        Mockito.verifyNoInteractions(userRepository, invitationRepository);
    }

    @Test
    void revokedInvitationBlocksVerification() {
        User user = unverifiedUser();
        when(userRepository.findWithAuthoritiesByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));
        when(invitationRepository.findFirstByEmailAndAcceptedAtIsNullAndRevokedAtIsNullOrderByCreatedAtDesc(EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verify(EMAIL, "123456")).isInstanceOf(ConflictException.class);
        assertThat(user.getEmailVerifiedAt()).isNull();
        assertThat(user.getAuthorities()).isEmpty();
    }

    @Test
    void alreadyVerifiedUserIsRejected() {
        User user = unverifiedUser();
        user.setEmailVerifiedAt(Instant.now());
        when(userRepository.findWithAuthoritiesByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.verify(EMAIL, "123456")).isInstanceOf(InvalidOtpException.class);
    }

    @Test
    void resendPropagatesRateLimit() {
        doThrow(new OtpRateLimitException(30)).when(otpService).requestEmailVerificationOtp(any(), any());

        assertThatThrownBy(() -> service.resendOtp(EMAIL)).isInstanceOf(OtpRateLimitException.class);
    }

    @Test
    void resendDeliversOnlyForUnverifiedUserWithOpenInvitation() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(unverifiedUser()));
        when(invitationRepository.existsByEmailAndAcceptedAtIsNullAndRevokedAtIsNull(EMAIL)).thenReturn(true);

        service.resendOtp(EMAIL);

        ArgumentCaptor<BooleanSupplier> captor = ArgumentCaptor.forClass(BooleanSupplier.class);
        Mockito.verify(otpService).requestEmailVerificationOtp(Mockito.eq(EMAIL), captor.capture());
        assertThat(captor.getValue().getAsBoolean()).isTrue();
    }

    @Test
    void resendDoesNotDeliverForUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        service.resendOtp(EMAIL);

        ArgumentCaptor<BooleanSupplier> captor = ArgumentCaptor.forClass(BooleanSupplier.class);
        Mockito.verify(otpService).requestEmailVerificationOtp(Mockito.eq(EMAIL), captor.capture());
        assertThat(captor.getValue().getAsBoolean()).isFalse();
    }
}
