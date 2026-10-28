package com.instello.backend.invitation;

import com.instello.backend.invitation.dto.AcceptInvitationRequest;
import com.instello.backend.invitation.dto.AcceptInvitationResponse;
import com.instello.backend.invitation.dto.InvitationPreviewResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/invitations")
@Tag(name = "auth/invitations", description = "Accept an owner invitation and create an account")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @GetMapping
    public ResponseEntity<InvitationPreviewResponse> preview(@RequestParam String token) {
        return ResponseEntity.ok(invitationService.preview(token));
    }

    @PostMapping("/accept")
    public ResponseEntity<AcceptInvitationResponse> accept(@Valid @RequestBody AcceptInvitationRequest requestBody) {
        return ResponseEntity.ok(invitationService.accept(requestBody.token(), requestBody.password()));
    }
}
