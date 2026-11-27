package in.instello.module.programSubject.service;

import in.instello.common.exceptions.BadRequestException;
import in.instello.common.exceptions.ConflictException;
import in.instello.common.exceptions.ResourceNotFoundException;
import in.instello.common.responses.PageResponse;
import in.instello.module.academicPattern.AcademicPatternService;
import in.instello.module.academicPattern.entity.AcademicStage;
import in.instello.module.institution.entity.Institution;
import in.instello.module.institution.repository.InstitutionRepository;
import in.instello.module.program.Program;
import in.instello.module.program.ProgramService;
import in.instello.module.programSubject.dto.*;
import in.instello.module.programSubject.entity.ProgramSubject;
import in.instello.module.programSubject.repository.ProgramSubjectRepository;
import in.instello.module.programSubject.repository.SubjectTypeRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
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
public class ProgramSubjectService {

  private ProgramService programService;
  private final AcademicPatternService academicPatternService;

  private final InstitutionRepository institutionRepository;
  private final ProgramSubjectRepository programSubjectRepository;
  private final SubjectTypeRepository subjectTypeRepository;

  private static final Set<String> SORTABLE_FIELDS = Set.of("name", "code", "alias");
  private static final int MAX_PAGE_SIZE = 100;

  public ProgramSubjectService(
      ProgramService programService,
      AcademicPatternService academicPatternService,
      InstitutionRepository institutionRepository,
      ProgramSubjectRepository programSubjectRepository,
      SubjectTypeRepository subjectTypeRepository) {
    this.programService = programService;
    this.academicPatternService = academicPatternService;
    this.institutionRepository = institutionRepository;
    this.programSubjectRepository = programSubjectRepository;
    this.subjectTypeRepository = subjectTypeRepository;
  }

  public AddProgramSubjectResponse add(
      AddProgramSubjectRequest addProgramSubjectRequest,
      String institutionIdOrSlug,
      String programIdOrAlias,
      String academicStageIdOrAlias) {

    Institution institution = findInstitution(institutionIdOrSlug);
    Program program = programService.findOrThrow(institutionIdOrSlug, programIdOrAlias);
    AcademicStage academicStage =
        academicPatternService.findOrThrow(institutionIdOrSlug, academicStageIdOrAlias);

    ProgramSubject newProgramSubject =
        programSubjectRepository.save(
            ProgramSubject.builder()
                .institution(institution)
                .program(program)
                .academicStage(academicStage)
                .subjectType(subjectType)
                .description(addProgramSubjectRequest.description())
                .build());
    return AddProgramSubjectResponse.fromSubject(newProgramSubject);
  }

  @Transactional(readOnly = true)
  public PageResponse<ProgramSubjectResponse> list(
      String institutionIdOrSlug, @Nullable String search, Pageable pageable) {
    Institution institution = findInstitution(institutionIdOrSlug);

    Specification<ProgramSubject> specification =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          predicates.add(
              cb.and(
                  cb.equal(root.get("institution"), institution), cb.isFalse(root.get("archive"))));

          String term = trimToNull(search);
          if (term != null) {
            String pattern = "%" + term.toLowerCase(Locale.ROOT) + "%";
            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("code")), pattern),
                    cb.like(cb.lower(root.get("alias")), pattern)));
          }
          return cb.and(predicates.toArray(Predicate[]::new));
        };

    Page<ProgramSubjectResponse> page =
        programSubjectRepository
            .findAll(specification, sanitize(pageable))
            .map(ProgramSubjectResponse::from);

    return PageResponse.from(page);
  }

  public ProgramSubjectResponse show(String institutionIdOrSlug, String subjectId) {
    Institution institution = findInstitution(institutionIdOrSlug);
    ProgramSubject programSubject = findOrThrow(institution, subjectId);

    return ProgramSubjectResponse.from(programSubject);
  }

  public ProgramSubjectArchiveResponse archiveSubjectType(
      String institutionIdOrSlug, String subjectTypeIdOrName) {

    Institution institution = findInstitution(institutionIdOrSlug);
    ProgramSubject programSubject = findOrThrow(institution, subjectTypeIdOrName);

    if (programSubject.isArchive()) {
      throw new BadRequestException("Subject Type archived already");
    }

    programSubject.setArchive(true);
    programSubjectRepository.save(programSubject);

    return ProgramSubjectArchiveResponse.from(programSubject);
  }

  public ProgramSubjectArchiveResponse unarchiveSubjectType(
      String institutionIdOrSlug, String subjectTypeIdOrName) {

    Institution institution = findInstitution(institutionIdOrSlug);
    ProgramSubject programSubject = findOrThrow(institution, subjectTypeIdOrName);

    if (!programSubject.isArchive()) {
      throw new BadRequestException("Subject Type unarchived already");
    }

    programSubject.setArchive(false);
    programSubjectRepository.save(programSubject);

    return ProgramSubjectArchiveResponse.from(programSubject);
  }

  @Transactional(readOnly = true)
  public PageResponse<ProgramSubjectResponse> listArchived(
      String institutionIdOrSlug, @Nullable String search, Pageable pageable) {
    Institution institution = findInstitution(institutionIdOrSlug);

    Specification<ProgramSubject> specification =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          predicates.add(
              cb.and(
                  cb.equal(root.get("institution"), institution), cb.isTrue(root.get("archive"))));

          String term = trimToNull(search);
          if (term != null) {
            String pattern = "%" + term.toLowerCase(Locale.ROOT) + "%";
            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("code")), pattern),
                    cb.like(cb.lower(root.get("alias")), pattern)));
          }
          return cb.and(predicates.toArray(Predicate[]::new));
        };

    Page<ProgramSubjectResponse> page =
        programSubjectRepository
            .findAll(specification, sanitize(pageable))
            .map(ProgramSubjectResponse::from);

    return PageResponse.from(page);
  }

  // Private Methods

  private Institution findInstitution(String institution) {
    return institutionRepository
        .findByIdOrSlug(institution, institution)
        .orElseThrow(() -> new ResourceNotFoundException("Institution Id not found"));
  }

  private String findSubjectAvailabilityOrThrow(Institution institution, String subjectIdorAlias) {
    ProgramSubject programSubject = find(institution, subjectIdorAlias);
    if (programSubject != null) throw new ConflictException("Subject Added exists already");
    return subjectIdorAlias;
  }

  private @Nullable ProgramSubject find(Institution institution, String subjectIdOrName) {
    return programSubjectRepository
        .findByIdOrNameInInstitution(subjectIdOrName, subjectIdOrName, institution)
        .orElse(null);
  }

  private ProgramSubject findOrThrow(Institution institution, String subjectId) {

    return programSubjectRepository
        .findByIdInInstitution(subjectId, subjectId, institution)
        .orElseThrow(() -> new ResourceNotFoundException("Subject Type not found"));
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

  private static @Nullable String trimToNull(@Nullable String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
