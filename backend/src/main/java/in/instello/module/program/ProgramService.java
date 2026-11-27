package in.instello.module.program;

import in.instello.common.exceptions.BadRequestException;
import in.instello.common.exceptions.ConflictException;
import in.instello.common.exceptions.ResourceNotFoundException;
import in.instello.common.responses.PageResponse;
import in.instello.module.institution.entity.Institution;
import in.instello.module.institution.repository.InstitutionRepository;
import in.instello.module.program.dto.CreateProgramRequest;
import in.instello.module.program.dto.CreateProgramResponse;
import in.instello.module.program.dto.ProgramAliasAvailabilityRequest;
import in.instello.module.program.dto.ProgramAliasAvailabilityResponse;
import in.instello.module.program.dto.ProgramArchiveResponse;
import in.instello.module.program.dto.ProgramResponse;
import in.instello.module.program.dto.UpdateProgramAliasRequest;
import in.instello.module.program.dto.UpdateProgramRequest;
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
public class ProgramService {

  private final InstitutionRepository institutionRepository;
  private final ProgramRepository programRepository;

  private static final Set<String> SORTABLE_FIELDS = Set.of("name", "code", "alias");
  private static final int MAX_PAGE_SIZE = 100;

  public ProgramService(
      InstitutionRepository institutionRepository, ProgramRepository programRepository) {
    this.institutionRepository = institutionRepository;
    this.programRepository = programRepository;
  }

  public CreateProgramResponse create(
      CreateProgramRequest createProgramRequest, String institutionIdOrSlug) {

    Institution institution = findInstitution(institutionIdOrSlug);

    String alias = findAliasAvailabilityOrThrow(institutionIdOrSlug, createProgramRequest.alias());

    Program newProgram =
        programRepository.save(
            Program.builder()
                .institution(institution)
                .name(createProgramRequest.name())
                .alias(alias)
                .build());
    return CreateProgramResponse.fromProgram(newProgram);
  }

  @Transactional(readOnly = true)
  public PageResponse<ProgramResponse> list(
      String institutionIdOrSlug, @Nullable String search, Pageable pageable) {
    Institution institution = findInstitution(institutionIdOrSlug);

    Specification<Program> specification =
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
                    cb.like(cb.lower(root.get("alias")), pattern)));
          }
          return cb.and(predicates.toArray(Predicate[]::new));
        };

    Page<ProgramResponse> page =
        programRepository.findAll(specification, sanitize(pageable)).map(ProgramResponse::from);

    return PageResponse.from(page);
  }

  public ProgramAliasAvailabilityResponse checkAlias(
      String institution, ProgramAliasAvailabilityRequest programAliasAvailabilityRequest) {
    Program program = find(institution, programAliasAvailabilityRequest.alias());
    return new ProgramAliasAvailabilityResponse(program != null ? false : true);
  }

  public ProgramResponse show(String institutionIdOrSlug, String programIdOrAlias) {
    Program program = findOrThrow(institutionIdOrSlug, programIdOrAlias);

    return ProgramResponse.from(program);
  }

  public ProgramResponse update(
      UpdateProgramRequest updateProgramRequest,
      String institutionIdOrSlug,
      String programIdOrAlias) {

    Program program = findOrThrow(institutionIdOrSlug, programIdOrAlias);

    program.setName(updateProgramRequest.name());
    programRepository.save(program);

    return ProgramResponse.from(program);
  }

  public ProgramResponse updateAlias(
      UpdateProgramAliasRequest updateProgramAliasRequest,
      String institutionIdOrSlug,
      String programIdOrAlias) {

    Program program = findOrThrow(institutionIdOrSlug, programIdOrAlias);

    String alias =
        findAliasAvailabilityOrThrow(institutionIdOrSlug, updateProgramAliasRequest.alias());

    program.setAlias(alias);

    programRepository.save(program);

    return ProgramResponse.from(program);
  }

  public ProgramArchiveResponse archive(String institutionIdOrSlug, String programIdOrAlias) {

    Program program = find(institutionIdOrSlug, programIdOrAlias);

    if (program.isArchive()) {
      throw new BadRequestException("Program archived already");
    }

    program.setArchive(true);
    programRepository.save(program);

    return ProgramArchiveResponse.from(program);
  }

  public ProgramArchiveResponse unarchive(String institutionIdOrSlug, String programIdOrAlias) {

    Program program = find(institutionIdOrSlug, programIdOrAlias);

    if (!program.isArchive()) {
      throw new BadRequestException("Program is not archived");
    }

    program.setArchive(false);
    programRepository.save(program);

    return ProgramArchiveResponse.from(program);
  }

  @Transactional(readOnly = true)
  public PageResponse<ProgramResponse> listArchives(
      String institutionIdOrSlug, @Nullable String search, Pageable pageable) {
    Institution institution = findInstitution(institutionIdOrSlug);

    Specification<Program> specification =
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
                    cb.like(cb.lower(root.get("alias")), pattern)));
          }
          return cb.and(predicates.toArray(Predicate[]::new));
        };

    Page<ProgramResponse> page =
        programRepository.findAll(specification, sanitize(pageable)).map(ProgramResponse::from);

    return PageResponse.from(page);
  }

  public Program findOrThrow(String institutionIdOrSlug, String programIdOrAlias) {
    Institution institution = findInstitution(institutionIdOrSlug);
    Program program =
        programRepository
            .findByIdOrAliasAndInstitution(programIdOrAlias, programIdOrAlias, institution)
            .orElseThrow(() -> new ResourceNotFoundException("Program not found"));

    return program;
  }

  // private methods starts here

  private Institution findInstitution(String institution) {
    return institutionRepository
        .findByIdOrSlug(institution, institution)
        .orElseThrow(() -> new ResourceNotFoundException("InstitutionId not found"));
  }

  private @Nullable Program find(String institutionIdOrSlug, String programIdOrAlias) {
    Institution institution = findInstitution(institutionIdOrSlug);
    return programRepository
        .findByIdOrAliasAndInstitution(programIdOrAlias, programIdOrAlias, institution)
        .orElse(null);
  }

  private String findAliasAvailabilityOrThrow(String institution, String alias) {
    Program program = find(institution, alias);
    if (program != null) throw new ConflictException("Alias exists already");
    return alias;
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
