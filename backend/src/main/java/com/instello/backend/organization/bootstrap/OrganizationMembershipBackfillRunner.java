package com.instello.backend.organization.bootstrap;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.instello.backend.organization.OrganizationMembershipService;

@Component
public class OrganizationMembershipBackfillRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationMembershipBackfillRunner.class);

    private final OrganizationMembershipService membershipService;

    public OrganizationMembershipBackfillRunner(OrganizationMembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {
        int created = membershipService.backfillAcceptedInvitations();
        if (created > 0) {
            logger.info("Backfilled {} organization memberships", created);
        }
    }
}
