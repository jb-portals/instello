package com.instello.backend.organization;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instello.backend.common.exception.ForbiddenException;
import com.instello.backend.common.exception.ResourceNotFoundException;
import com.instello.backend.invitation.entity.Invitation;
import com.instello.backend.invitation.repository.InvitationRepository;
import com.instello.backend.organization.entity.Organization;
import com.instello.backend.organization.entity.OrganizationMember;
import com.instello.backend.organization.repository.OrganizationMemberRepository;
import com.instello.backend.organization.repository.OrganizationRepository;
import com.instello.backend.user.entity.AppAuthority;
import com.instello.backend.user.entity.User;
import com.instello.backend.user.repository.UserRepository;

@Service
public class OrganizationMembershipService {

    private final OrganizationMemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;

    public OrganizationMembershipService(
            OrganizationMemberRepository memberRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            InvitationRepository invitationRepository
    ) {
        this.memberRepository = memberRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
    }

    @Transactional
    public boolean ensureMember(User user, Organization organization, AppAuthority role) {
        if (memberRepository.existsByUser_IdAndOrganization_Id(user.getId(), organization.getId())) {
            return false;
        }
        memberRepository.save(OrganizationMember.builder()
                .user(user)
                .organization(organization)
                .role(role)
                .build());
        return true;
    }

    @Transactional(readOnly = true)
    public void requireMember(UUID organizationId, String email) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found");
        }
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ForbiddenException("You do not have access to this organization"));
        if (!memberRepository.existsByUser_IdAndOrganization_Id(user.getId(), organizationId)) {
            throw new ForbiddenException("You do not have access to this organization");
        }
    }

    /**
     * Creates memberships for invitations accepted before membership rows existed.
     *
     * @return how many memberships were created
     */
    @Transactional
    public int backfillAcceptedInvitations() {
        int created = 0;
        for (Invitation invitation : invitationRepository.findByAcceptedAtIsNotNull()) {
            if (invitation.getOrganization() == null) {
                continue;
            }
            User user = userRepository.findByEmailIgnoreCase(invitation.getEmail()).orElse(null);
            if (user != null && ensureMember(user, invitation.getOrganization(), invitation.getRole())) {
                created++;
            }
        }
        return created;
    }
}
