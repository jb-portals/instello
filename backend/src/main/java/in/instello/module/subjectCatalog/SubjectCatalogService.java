package in.instello.module.subjectCatalog;

import in.instello.common.exceptions.BadRequestException;
import in.instello.common.exceptions.ConflictException;
import in.instello.common.exceptions.ResourceNotFoundException;
import in.instello.common.responses.PageResponse;
import in.instello.module.institution.entity.Institution;
import in.instello.module.institution.repository.InstitutionRepository;
import in.instello.module.subjectCatalog.dto.CreateSubjectRequest;
import in.instello.module.subjectCatalog.dto.CreateSubjectResponse;
import in.instello.module.subjectCatalog.dto.SubjectAliasAvailabilityRequest;
import in.instello.module.subjectCatalog.dto.SubjectAliasAvailabilityResponse;
import in.instello.module.subjectCatalog.dto.SubjectArchiveResponse;
import in.instello.module.subjectCatalog.dto.SubjectResponse;
import in.instello.module.subjectCatalog.dto.UpdateSubjectRequest;
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
public class SubjectCatalogService {
  private final InstitutionRepository institutionRepository;
  private final SubjectRepository subjectRepository;

  private static final Set<String> SORTABLE_FIELDS = Set.of("name", "code", "alias");
  private static final int MAX_PAGE_SIZE = 100;

  public SubjectCatalogService(
      InstitutionRepository institutionRepository, SubjectRepository subjectRepository) {
    this.institutionRepository = institutionRepository;
    this.subjectRepository = subjectRepository;
  }

  public CreateSubjectResponse create(
      CreateSubjectRequest createSubjectRequest, String institutionIdOrSlug) {

    Institution institution = findInstitution(institutionIdOrSlug);

    Subject newSubject =
        subjectRepository.save(
            Subject.builder()
                .institution(institution)
                .name(createSubjectRequest.name())
                .code(createSubjectRequest.code())
                .alias(createSubjectRequest.alias())
                .colorHash(createSubjectRequest.colorHash())
                .description(createSubjectRequest.description())
                .build());
    return CreateSubjectResponse.fromSubject(newSubject);
  }

  public SubjectAliasAvailabilityResponse checkAlias(
      String institution, SubjectAliasAvailabilityRequest subjectAliasAvailabilityRequest) {
    Subject subject = find(institution, subjectAliasAvailabilityRequest.alias());
    return new SubjectAliasAvailabilityResponse(subject != null ? false : true);
  }

  @Transactional(readOnly = true)
  public PageResponse<SubjectResponse> list(
      String institutionIdOrSlug, @Nullable String search, Pageable pageable) {
    Institution institution = findInstitution(institutionIdOrSlug);

    Specification<Subject> specification =
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

    Page<SubjectResponse> page =
        subjectRepository.findAll(specification, sanitize(pageable)).map(SubjectResponse::from);

    return PageResponse.from(page);
  }

  public SubjectResponse show(String institutionIdOrSlug, String subjectIdOrAlias) {
    Subject subject = findOrThrow(institutionIdOrSlug, subjectIdOrAlias);

    return SubjectResponse.from(subject);
  }

  public SubjectResponse update(
      UpdateSubjectRequest updateSubjectRequest,
      String institutionIdOrSlug,
      String subjectIdOrAlias) {
    Subject subject = findOrThrow(institutionIdOrSlug, subjectIdOrAlias);

    updateSubjectRequest.applyTo(subject);
    subjectRepository.save(subject);

    return SubjectResponse.from(subject);
  }

  public SubjectArchiveResponse archive(String institutionIdOrSlug, String subjectIdOrAlias) {

    Subject subject = findOrThrow(institutionIdOrSlug, subjectIdOrAlias);

    if (subject.isArchive()) {
      throw new BadRequestException("Subject archived already");
    }

    subject.setArchive(true);
    subjectRepository.save(subject);

    return SubjectArchiveResponse.from(subject);
  }

  public SubjectArchiveResponse unarchive(String institutionIdOrSlug, String subjectIdOrAlias) {

    Subject subject = findOrThrow(institutionIdOrSlug, subjectIdOrAlias);

    if (!subject.isArchive()) {
      throw new BadRequestException("Subject is not archived");
    }

    subject.setArchive(false);
    subjectRepository.save(subject);

    return SubjectArchiveResponse.from(subject);
  }

  @Transactional(readOnly = true)
  public PageResponse<SubjectResponse> listArchives(
      String institutionIdOrSlug, @Nullable String search, Pageable pageable) {
    Institution institution = findInstitution(institutionIdOrSlug);

    Specification<Subject> specification =
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

    Page<SubjectResponse> page =
        subjectRepository.findAll(specification, sanitize(pageable)).map(SubjectResponse::from);

    return PageResponse.from(page);
  }

  private static @Nullable String trimToNull(@Nullable String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private Institution findInstitution(String institution) {
    return institutionRepository
        .findByIdOrSlug(institution, institution)
        .orElseThrow(() -> new ResourceNotFoundException("InstitutionId not found"));
  }

  private @Nullable Subject find(String institutionIdOrSlug, String subjectIdOrAlias) {
    Institution institution = findInstitution(institutionIdOrSlug);
    return subjectRepository
        .findByIdOrAliasAndInstitution(subjectIdOrAlias, subjectIdOrAlias, institution)
        .orElse(null);
  }

  private Subject findOrThrow(String institutionIdOrSlug, String subjectIdOrAlias) {
    Institution institution = findInstitution(institutionIdOrSlug);
    Subject subject =
        subjectRepository
            .findByIdOrAliasAndInstitution(subjectIdOrAlias, subjectIdOrAlias, institution)
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

    return subject;
  }

  private String findAliasAvailabilityOrThrow(String institutionIdOrSlug, String alias) {
    Subject subject = find(institutionIdOrSlug, alias);
    if (subject != null) throw new ConflictException("Alias exists already");
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
}
