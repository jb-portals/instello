package com.instello.backend.invitation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.instello.backend.accessRequest.entity.AccessRequest;
import com.instello.backend.auth.otp.OtpService;
import com.instello.backend.common.exception.ConflictException;
import com.instello.backend.common.exception.ResourceNotFoundException;
import com.instello.backend.common.notification.email.EmailTemplateService;
import com.instello.backend.invitation.dto.AcceptInvitationResponse;
import com.instello.backend.invitation.entity.Invitation;
import com.instello.backend.invitation.repository.InvitationRepository;
import com.instello.backend.organization.entity.Organization;
import com.instello.backend.user.entity.AppAuthority;
import com.instello.backend.user.entity.User;
import com.instello.backend.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class InvitationServiceAcceptTest {

    private static final String TOKEN = "raw-token";
    private static final String EMAIL = "owner@example.com";

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private EmailTemplateService emailTemplateService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OtpService otpService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private InvitationService service;

    @BeforeEach
    void setUp() {
        service = new InvitationService(
                invitationRepository,
                emailTemplateService,
                new InvitationProperties(Duration.ofDays(7), "http://localhost/signup"),
                userRepository,
                passwordEncoder,
                otpService
        );
    }

    private Invitation invitation() {
        return Invitation.builder()
                .email(EMAIL)
                .organization(Organization.builder().name("Acme").build())
                .role(AppAuthority.ROLE_OWNER)
                .accessRequest(AccessRequest.builder().firstName("Olivia").lastName("Owner").build())
                .metadata(Map.of("email", EMAIL))
                .tokenHash(InvitationService.hash(TOKEN))
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .build();
    }

    private void givenInvitation(Invitation invitation) {
        when(invitationRepository.findByTokenHash(InvitationService.hash(TOKEN))).thenReturn(Optional.of(invitation));
    }

    @Test
    void createsUnverifiedUserWithHashedPasswordAndSendsOtp() {
        givenInvitation(invitation());
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        AcceptInvitationResponse response = service.accept(TOKEN, "s3cret-password");

        assertThat(response.email()).isEqualTo(EMAIL);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getEmailVerifiedAt()).isNull();
        assertThat(saved.getAuthorities()).isEmpty();
        assertThat(saved.getPasswordHash()).isNotEqualTo("s3cret-password");
        assertThat(passwordEncoder.matches("s3cret-password", saved.getPasswordHash())).isTrue();
        assertThat(saved.getUserProfile().getFirstName()).isEqualTo("Olivia");
        verify(otpService).requestEmailVerificationOtp(any(), any());
    }

    @Test
    void unknownToken() {
        when(invitationRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.accept(TOKEN, "s3cret-password"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void expiredInvitationIsRejected() {
        Invitation invitation = invitation();
        invitation.setExpiresAt(Instant.now().minusSeconds(5));
        givenInvitation(invitation);

        assertThatThrownBy(() -> service.accept(TOKEN, "s3cret-password"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void revokedInvitationIsRejected() {
        Invitation invitation = invitation();
        invitation.setRevokedAt(Instant.now());
        givenInvitation(invitation);

        assertThatThrownBy(() -> service.accept(TOKEN, "s3cret-password"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void missingMetadataFailsIntegrityCheck() {
        Invitation invitation = invitation();
        invitation.setMetadata(null);
        givenInvitation(invitation);

        assertThatThrownBy(() -> service.accept(TOKEN, "s3cret-password"))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void metadataEmailMismatchFailsIntegrityCheck() {
        Invitation invitation = invitation();
        invitation.setMetadata(Map.of("email", "someone-else@example.com"));
        givenInvitation(invitation);

        assertThatThrownBy(() -> service.accept(TOKEN, "s3cret-password"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void verifiedUserAlreadyExists() {
        givenInvitation(invitation());
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(
                User.builder().email(EMAIL).emailVerifiedAt(Instant.now()).build()));

        assertThatThrownBy(() -> service.accept(TOKEN, "s3cret-password"))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void retryOverwritesPasswordOfUnverifiedUser() {
        givenInvitation(invitation());
        User existing = User.builder()
                .email(EMAIL)
                .passwordHash(passwordEncoder.encode("old-password"))
                .authorities(new ArrayList<>())
                .build();
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(existing));

        service.accept(TOKEN, "new-password-1");

        verify(userRepository).saveAndFlush(existing);
        assertThat(passwordEncoder.matches("new-password-1", existing.getPasswordHash())).isTrue();
        verify(otpService).requestEmailVerificationOtp(any(), any());
    }
}
