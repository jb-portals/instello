package com.instello.backend.accessRequest;

import com.instello.backend.accessRequest.dto.AccessRequestDetail;
import com.instello.backend.accessRequest.dto.AccessRequestSummary;
import com.instello.backend.accessRequest.dto.ApproveAccessRequestRequest;
import com.instello.backend.accessRequest.dto.CreateAccessRequestRequest;
import com.instello.backend.accessRequest.dto.CreateAccessRequestResponse;
import com.instello.backend.accessRequest.dto.RejectAccessRequestRequest;
import com.instello.backend.accessRequest.dto.UpdateAccessRequestRequest;
import com.instello.backend.accessRequest.entity.AccessRequestStatus;
import com.instello.backend.common.response.PageResponse;
import com.instello.backend.common.web.ApiVersions;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/access-requests")
@Tag(name = "accessRequests")
public class AccessRequestController {

    private final AccessRequestService accessRequestService;

    public AccessRequestController(AccessRequestService accessRequestService) {
        this.accessRequestService = accessRequestService;
    }

    @PostMapping
    public ResponseEntity<CreateAccessRequestResponse> submit(
            @Valid @RequestBody CreateAccessRequestRequest requestBody,
            HttpServletRequest request
    ) {
        CreateAccessRequestResponse response = accessRequestService.submit(requestBody, request.getRemoteAddr());

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/v{version}/access-requests/{id}")
                .buildAndExpand(ApiVersions.V0_1, response.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, location.toString())
                .body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<AccessRequestSummary>> list(
            @RequestParam(required = false) AccessRequestStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(accessRequestService.list(status, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccessRequestDetail> get(@PathVariable UUID id) {
        return ResponseEntity.ok(accessRequestService.get(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AccessRequestDetail> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccessRequestRequest requestBody,
            Authentication authentication
    ) {
        return ResponseEntity.ok(accessRequestService.update(id, requestBody, authentication.getName()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<AccessRequestDetail> reject(
            @PathVariable UUID id,
            @Valid @RequestBody RejectAccessRequestRequest requestBody,
            Authentication authentication
    ) {
        return ResponseEntity.ok(accessRequestService.reject(id, requestBody.reason(), authentication.getName()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<AccessRequestDetail> approve(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) ApproveAccessRequestRequest requestBody,
            Authentication authentication
    ) {
        return ResponseEntity.ok(accessRequestService.approve(id, requestBody, authentication.getName()));
    }

    @PostMapping("/{id}/resend-invitation")
    public ResponseEntity<AccessRequestDetail> resendInvitation(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(accessRequestService.resendInvitation(id, authentication.getName()));
    }
}
