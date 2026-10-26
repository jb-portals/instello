package com.instello.backend.invitation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.instello.backend.accessRequest.entity.AccessRequest;
import com.instello.backend.common.notification.email.EmailTemplateService;
import com.instello.backend.invitation.entity.Invitation;
import com.instello.backend.invitation.repository.InvitationRepository;
import com.instello.backend.organization.entity.Organization;
import com.instello.backend.user.entity.PlatformRole;
import com.instello.backend.user.entity.User;

@Service
public class InvitationService {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final InvitationRepository invitationRepository;
    private final EmailTemplateService emailTemplateService;
    private final InvitationProperties properties;

    public InvitationService(
            InvitationRepository invitationRepository,
            EmailTemplateService emailTemplateService,
            InvitationProperties properties
    ) {
        this.invitationRepository = invitationRepository;
        this.emailTemplateService = emailTemplateService;
        this.properties = properties;
    }

    @Transactional
    public Invitation createOwnerInvitation(
            Organization organization,
            String email,
            String firstName,
            AccessRequest accessRequest,
            User invitedBy
    ) {
        String rawToken = generateToken();

        Invitation invitation = invitationRepository.save(Invitation.builder()
                .email(email.trim().toLowerCase(Locale.ROOT))
                .organization(organization)
                .role(PlatformRole.ORG_OWNER)
                .accessRequest(accessRequest)
                .tokenHash(hash(rawToken))
                .expiresAt(Instant.now().plus(properties.ttl()))
                .invitedBy(invitedBy)
                .build());

        String inviteUrl = UriComponentsBuilder.fromUriString(properties.acceptUrl())
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        emailTemplateService.sendInvitation(
                invitation.getEmail(),
                firstName,
                organization.getName(),
                inviteUrl,
                (int) properties.ttl().toDays()
        );

        return invitation;
    }

    @Transactional
    public void revokeActive(UUID accessRequestId) {
        Instant now = Instant.now();
        invitationRepository.findByAccessRequestIdAndAcceptedAtIsNullAndRevokedAtIsNull(accessRequestId)
                .forEach(invitation -> invitation.setRevokedAt(now));
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
