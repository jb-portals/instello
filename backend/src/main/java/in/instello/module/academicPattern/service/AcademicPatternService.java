package in.instello.module.academicPattern.service;

import in.instello.common.exceptions.BadRequestException;
import in.instello.common.exceptions.ConflictException;
import in.instello.common.exceptions.ResourceNotFoundException;
import in.instello.module.academicPattern.dto.AcademicPatternResponse;
import in.instello.module.academicPattern.dto.AcademicStageResponse;
import in.instello.module.academicPattern.dto.CreateAcademicPatternRequest;
import in.instello.module.academicPattern.dto.UpdateAcademicPatternRequest;
import in.instello.module.academicPattern.entity.AcademicPattern;
import in.instello.module.academicPattern.repository.AcademicPatternRepository;
import in.instello.module.institution.repository.InstitutionRepository;
import in.instello.module.organization.OrganizationMembershipService;
import in.instello.module.organization.repository.OrganizationRepository;
import java.util.List;
import java.util.Set;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademicPatternService {

  private static final String LOCKED_MESSAGE =
      "Pattern is locked because an institution uses this pattern";
  private static final String DUPLICATE_NAME = "An academic pattern already uses this name";

  private final AcademicPatternRepository academicPatternRepository;
  private final AcademicStageService academicStageService;
  private final InstitutionRepository institutionRepository;
  private final OrganizationRepository organizationRepository;
  private final OrganizationMembershipService organizationMembershipService;

  AcademicPatternService(
      AcademicPatternRepository academicPatternRepository,
      AcademicStageService academicStageService,
      InstitutionRepository institutionRepository,
      OrganizationRepository organizationRepository,
      OrganizationMembershipService organizationMembershipService) {
    this.academicPatternRepository = academicPatternRepository;
    this.academicStageService = academicStageService;
    this.institutionRepository = institutionRepository;
    this.organizationRepository = organizationRepository;
    this.organizationMembershipService = organizationMembershipService;
  }

  @Transactional(readOnly = true)
  public List<AcademicPatternResponse> list(String organizationId, String email) {
    organizationMembershipService.requireMember(organizationId, email);
    List<AcademicPattern> results =
        academicPatternRepository.findByOrganization_IdOrderByNameAsc(organizationId);
    if (results.isEmpty()) {
      return List.of();
    }
    Set<String> lockedIds =
        institutionRepository.findReferencedAcademicPatternIds(
            results.stream().map(AcademicPattern::getId).toList());
    return results.stream()
        .map(
            pattern ->
                response(
                    pattern,
                    lockedIds.contains(pattern.getId()),
                    academicStageService.list(pattern)))
        .toList();
  }

  @Transactional(readOnly = true)
  public AcademicPatternResponse get(String organizationId, String patternId, String email) {
    organizationMembershipService.requireMember(organizationId, email);
    AcademicPattern pattern = findPattern(organizationId, patternId);
    return response(pattern, isLocked(patternId), academicStageService.list(pattern));
  }

  @Transactional
  public AcademicPatternResponse create(
      String organizationId, CreateAcademicPatternRequest request, String email) {
    organizationMembershipService.requireOwner(organizationId, email);
    String name = requiredName(request.name());
    if (academicPatternRepository.existsByOrganization_IdAndName(organizationId, name)) {
      throw new ConflictException(DUPLICATE_NAME);
    }
    AcademicPattern pattern =
        save(
            AcademicPattern.builder()
                .organization(organizationRepository.getReferenceById(organizationId))
                .name(name)
                .description(normalizeDescription(request.description()))
                .durationInYears(request.durationInYears())
                .systemType(request.systemType())
                .build());
    return response(
        pattern,
        false,
        academicStageService.responses(pattern, academicStageService.generate(pattern)));
  }

  @Transactional
  public AcademicPatternResponse update(
      String organizationId, String patternId, UpdateAcademicPatternRequest request, String email) {
    organizationMembershipService.requireOwner(organizationId, email);
    AcademicPattern pattern = findPattern(organizationId, patternId);
    if (request.name() != null) {
      String name = requiredName(request.name());
      if (!name.equals(pattern.getName())
          && academicPatternRepository.existsByOrganization_IdAndName(organizationId, name)) {
        throw new ConflictException(DUPLICATE_NAME);
      }
      pattern.setName(name);
    }
    if (request.description() != null) {
      pattern.setDescription(normalizeDescription(request.description()));
    }
    save(pattern);
    return response(pattern, isLocked(patternId), academicStageService.list(pattern));
  }

  @Transactional
  public void delete(String organizationId, String patternId, String email) {
    organizationMembershipService.requireOwner(organizationId, email);
    AcademicPattern pattern = findPattern(organizationId, patternId);
    if (isLocked(patternId)) {
      throw new ConflictException(LOCKED_MESSAGE);
    }

    academicStageService.deleteAll(patternId);
    try {
      academicPatternRepository.delete(pattern);
      academicPatternRepository.flush();
    } catch (DataIntegrityViolationException exception) {
      // PostgreSQL can reject the delete if adoption committed after the lock check.
      if (hasSqlState(exception, "23503")) {
        throw new ConflictException(LOCKED_MESSAGE);
      }
      throw exception;
    }
  }

  private AcademicPattern save(AcademicPattern pattern) {
    try {
      return academicPatternRepository.saveAndFlush(pattern);
    } catch (DataIntegrityViolationException exception) {
      if (hasSqlState(exception, "23505")) {
        throw new ConflictException(DUPLICATE_NAME);
      }
      throw exception;
    }
  }

  private boolean isLocked(String patternId) {
    return institutionRepository.existsByAcademicPattern_Id(patternId);
  }

  private AcademicPattern findPattern(String organizationId, String patternId) {
    return academicPatternRepository
        .findByIdAndOrganization_Id(patternId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Academic pattern not found"));
  }

  private static AcademicPatternResponse response(
      AcademicPattern pattern, boolean locked, List<AcademicStageResponse> stageResponses) {
    return new AcademicPatternResponse(
        pattern.getId(),
        pattern.getOrganization().getId(),
        pattern.getName(),
        pattern.getDescription(),
        pattern.getDurationInYears(),
        pattern.getSystemType(),
        locked,
        !locked,
        stageResponses,
        pattern.getCreatedAt(),
        pattern.getUpdatedAt());
  }

  private static boolean hasSqlState(Throwable exception, String sqlState) {
    for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
      if (cause instanceof ConstraintViolationException violation
          && sqlState.equals(violation.getSQLState())) {
        return true;
      }
    }
    return false;
  }

  private static String requiredName(String value) {
    String name = value.trim();
    if (name.isEmpty()) {
      throw new BadRequestException("Name must not be blank");
    }
    return name;
  }

  private static @Nullable String normalizeDescription(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
