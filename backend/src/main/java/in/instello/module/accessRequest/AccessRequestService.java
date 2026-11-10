package in.instello.module.accessRequest;

import in.instello.common.address.Address;
import in.instello.common.address.dto.AddressResponse;
import in.instello.common.exception.app.BadRequestException;
import in.instello.common.exception.app.ConflictException;
import in.instello.common.exception.app.RateLimitExceededException;
import in.instello.common.exception.app.ResourceNotFoundException;
import in.instello.common.exception.app.UnauthorizedException;
import in.instello.common.exception.response.PageResponse;
import in.instello.module.accessRequest.dto.AccessRequestDetail;
import in.instello.module.accessRequest.dto.AccessRequestSummary;
import in.instello.module.accessRequest.dto.ApproveAccessRequestRequest;
import in.instello.module.accessRequest.dto.CreateAccessRequestRequest;
import in.instello.module.accessRequest.dto.CreateAccessRequestResponse;
import in.instello.module.accessRequest.dto.UpdateAccessRequestRequest;
import in.instello.module.accessRequest.entity.AccessRequest;
import in.instello.module.accessRequest.entity.AccessRequestStatus;
import in.instello.module.accessRequest.repository.AccessRequestRepository;
import in.instello.module.invitation.InvitationService;
import in.instello.module.invitation.entity.Invitation;
import in.instello.module.invitation.repository.InvitationRepository;
import in.instello.module.organization.OrganizationService;
import in.instello.module.organization.entity.Organization;
import in.instello.module.user.entity.User;
import in.instello.module.user.repository.UserRepository;
import in.instello.redis.RedisService;
import jakarta.persistence.criteria.Predicate;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessRequestService {

  private static final Set<AccessRequestStatus> OPEN_STATUSES =
      EnumSet.of(AccessRequestStatus.PENDING, AccessRequestStatus.CONTACTED);
  private static final Set<String> SORTABLE_FIELDS =
      Set.of("createdAt", "updatedAt", "status", "organizationName", "email");
  private static final int MAX_PAGE_SIZE = 100;

  private static final Duration IP_WINDOW = Duration.ofHours(1);
  private static final int MAX_REQUESTS_PER_IP = 5;
  private static final Duration EMAIL_WINDOW = Duration.ofDays(1);
  private static final int MAX_REQUESTS_PER_EMAIL = 3;

  private final AccessRequestRepository accessRequestRepository;
  private final InvitationRepository invitationRepository;
  private final UserRepository userRepository;
  private final OrganizationService organizationService;
  private final InvitationService invitationService;
  private final RedisService redisService;

  public AccessRequestService(
      AccessRequestRepository accessRequestRepository,
      InvitationRepository invitationRepository,
      UserRepository userRepository,
      OrganizationService organizationService,
      InvitationService invitationService,
      RedisService redisService) {
    this.accessRequestRepository = accessRequestRepository;
    this.invitationRepository = invitationRepository;
    this.userRepository = userRepository;
    this.organizationService = organizationService;
    this.invitationService = invitationService;
    this.redisService = redisService;
  }

  @Transactional
  public CreateAccessRequestResponse submit(CreateAccessRequestRequest body, String clientIp) {
    String email = normalizeEmail(body.email());

    enforceRateLimit("access-request:ip:" + clientIp, IP_WINDOW, MAX_REQUESTS_PER_IP);
    enforceRateLimit("access-request:email:" + email, EMAIL_WINDOW, MAX_REQUESTS_PER_EMAIL);

    if (accessRequestRepository.existsByEmailIgnoreCaseAndStatusIn(email, OPEN_STATUSES)) {
      throw new ConflictException("A request with this email is already under review");
    }

    AccessRequest accessRequest =
        accessRequestRepository.save(
            AccessRequest.builder()
                .organizationName(body.organizationName().trim())
                .firstName(body.firstName().trim())
                .lastName(body.lastName().trim())
                .email(email)
                .phone(body.phone().trim())
                .address(body.address().toAddress())
                .message(trimToNull(body.message()))
                .build());

    return new CreateAccessRequestResponse(accessRequest.getId(), accessRequest.getStatus());
  }

  @Transactional(readOnly = true)
  public PageResponse<AccessRequestSummary> list(
      AccessRequestStatus status, String search, Pageable pageable) {
    Specification<AccessRequest> specification =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
          }
          String term = trimToNull(search);
          if (term != null) {
            String pattern = "%" + term.toLowerCase(Locale.ROOT) + "%";
            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("organizationName")), pattern),
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)));
          }
          return cb.and(predicates.toArray(Predicate[]::new));
        };

    Page<AccessRequestSummary> page =
        accessRequestRepository
            .findAll(specification, sanitize(pageable))
            .map(AccessRequestSummary::from);

    return PageResponse.from(page);
  }

  @Transactional(readOnly = true)
  public AccessRequestDetail get(String id) {
    return toDetail(findById(id));
  }

  @Transactional
  public AccessRequestDetail update(String id, UpdateAccessRequestRequest body, String adminEmail) {
    AccessRequest accessRequest = findById(id);
    requireOpen(accessRequest);

    if (body.status() != null) {
      if (!OPEN_STATUSES.contains(body.status())) {
        throw new BadRequestException("Use the approve or reject endpoint to close a request");
      }
      accessRequest.setStatus(body.status());
    }
    if (body.adminNotes() != null) {
      accessRequest.setAdminNotes(trimToNull(body.adminNotes()));
    }
    accessRequest.setReviewedBy(findAdmin(adminEmail));
    accessRequest.setReviewedAt(Instant.now());

    return toDetail(accessRequest);
  }

  @Transactional
  public AccessRequestDetail reject(String id, String reason, String adminEmail) {
    AccessRequest accessRequest = findById(id);
    requireOpen(accessRequest);

    accessRequest.setStatus(AccessRequestStatus.REJECTED);
    accessRequest.setRejectionReason(reason.trim());
    accessRequest.setReviewedBy(findAdmin(adminEmail));
    accessRequest.setReviewedAt(Instant.now());

    return toDetail(accessRequest);
  }

  @Transactional
  public AccessRequestDetail approve(
      String id, @Nullable ApproveAccessRequestRequest body, String adminEmail) {
    AccessRequest accessRequest = findById(id);
    requireOpen(accessRequest);
    User admin = findAdmin(adminEmail);

    ApproveAccessRequestRequest overrides =
        body != null ? body : new ApproveAccessRequestRequest(null, null, null, null);
    Address address =
        overrides.address() != null ? overrides.address().toAddress() : accessRequest.getAddress();
    if (address == null) {
      throw new BadRequestException("An address is required to approve an access request");
    }

    Organization organization =
        organizationService.create(
            firstNonBlank(overrides.organizationName(), accessRequest.getOrganizationName()),
            overrides.email() != null
                ? normalizeEmail(overrides.email())
                : accessRequest.getEmail(),
            firstNonBlank(overrides.phone(), accessRequest.getPhone()),
            address,
            admin);

    accessRequest.setStatus(AccessRequestStatus.APPROVED);
    accessRequest.setOrganization(organization);
    accessRequest.setReviewedBy(admin);
    accessRequest.setReviewedAt(Instant.now());

    invitationService.createOwnerInvitation(
        organization, accessRequest.getEmail(), accessRequest.getFirstName(), accessRequest, admin);

    return toDetail(accessRequest);
  }

  @Transactional
  public AccessRequestDetail resendInvitation(String id, String adminEmail) {
    AccessRequest accessRequest = findById(id);
    if (accessRequest.getStatus() != AccessRequestStatus.APPROVED
        || accessRequest.getOrganization() == null) {
      throw new ConflictException("Only approved requests can have their invitation resent");
    }
    boolean alreadyAccepted =
        invitationRepository
            .findFirstByAccessRequestIdOrderByCreatedAtDesc(id)
            .map(invitation -> invitation.getAcceptedAt() != null)
            .orElse(false);
    if (alreadyAccepted) {
      throw new ConflictException("The invitation has already been accepted");
    }

    invitationService.revokeActive(id);
    invitationService.createOwnerInvitation(
        accessRequest.getOrganization(),
        accessRequest.getEmail(),
        accessRequest.getFirstName(),
        accessRequest,
        findAdmin(adminEmail));

    return toDetail(accessRequest);
  }

  private AccessRequest findById(String id) {
    return accessRequestRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Access request not found"));
  }

  private User findAdmin(String adminEmail) {
    return userRepository
        .findByEmailIgnoreCase(adminEmail)
        .orElseThrow(() -> new UnauthorizedException("User no longer exists"));
  }

  private static void requireOpen(AccessRequest accessRequest) {
    if (!accessRequest.getStatus().isOpen()) {
      throw new ConflictException(
          "Access request is already " + accessRequest.getStatus().name().toLowerCase(Locale.ROOT));
    }
  }

  private void enforceRateLimit(String key, Duration window, int max) {
    if (redisService.increment(key, window) > max) {
      throw new RateLimitExceededException(
          "Too many access requests. Try again later.",
          Math.max(1, redisService.getRemainingExpiration(key)));
    }
  }

  private AccessRequestDetail toDetail(AccessRequest accessRequest) {
    Organization organization = accessRequest.getOrganization();
    Invitation invitation =
        invitationRepository
            .findFirstByAccessRequestIdOrderByCreatedAtDesc(accessRequest.getId())
            .orElse(null);

    return new AccessRequestDetail(
        accessRequest.getId(),
        accessRequest.getOrganizationName(),
        accessRequest.getFirstName(),
        accessRequest.getLastName(),
        accessRequest.getEmail(),
        accessRequest.getPhone(),
        AddressResponse.from(accessRequest.getAddress()),
        accessRequest.getMessage(),
        accessRequest.getStatus(),
        accessRequest.getAdminNotes(),
        accessRequest.getRejectionReason(),
        accessRequest.getReviewedBy() != null ? accessRequest.getReviewedBy().getEmail() : null,
        accessRequest.getReviewedAt(),
        organization != null
            ? new AccessRequestDetail.OrganizationInfo(organization.getId(), organization.getName())
            : null,
        invitation != null
            ? new AccessRequestDetail.InvitationInfo(
                invitation.getId(),
                invitation.getEmail(),
                invitation.getStatus(),
                invitation.getExpiresAt(),
                invitation.getCreatedAt())
            : null,
        accessRequest.getCreatedAt(),
        accessRequest.getUpdatedAt());
  }

  private static Pageable sanitize(Pageable pageable) {
    List<Sort.Order> orders =
        pageable.getSort().stream()
            .filter(order -> SORTABLE_FIELDS.contains(order.getProperty()))
            .toList();
    Sort sort = orders.isEmpty() ? Sort.by(Sort.Direction.DESC, "createdAt") : Sort.by(orders);

    return PageRequest.of(
        pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE), sort);
  }

  private static Address copy(Address address) {
    return Address.builder()
        .lineOne(address.getLineOne())
        .lineTwo(address.getLineTwo())
        .city(address.getCity())
        .state(address.getState())
        .country(address.getCountry())
        .build();
  }

  private static String firstNonBlank(@Nullable String value, String fallback) {
    String trimmed = trimToNull(value);
    return trimmed != null ? trimmed : fallback;
  }

  private static @Nullable String trimToNull(@Nullable String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private static String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
