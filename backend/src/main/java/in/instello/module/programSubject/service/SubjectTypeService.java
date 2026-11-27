package in.instello.module.programSubject.service;

import in.instello.common.exceptions.BadRequestException;
import in.instello.common.exceptions.ConflictException;
import in.instello.common.exceptions.ResourceNotFoundException;
import in.instello.common.responses.PageResponse;
import in.instello.module.institution.entity.Institution;
import in.instello.module.institution.repository.InstitutionRepository;
import in.instello.module.programSubject.dto.CreateSubjectTypeRequest;
import in.instello.module.programSubject.dto.CreateSubjectTypeResponse;
import in.instello.module.programSubject.dto.SubjectTypeArchiveResponse;
import in.instello.module.programSubject.dto.SubjectTypeResponse;
import in.instello.module.programSubject.dto.UpdateSubjectTypeNameRequest;
import in.instello.module.programSubject.entity.SubjectType;
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
public class SubjectTypeService {

  private final InstitutionRepository institutionRepository;
  private final SubjectTypeRepository subjectTypeRepository;

  private static final Set<String> SORTABLE_FIELDS = Set.of("name", "code", "alias");
  private static final int MAX_PAGE_SIZE = 100;

  public SubjectTypeService(
      InstitutionRepository institutionRepository, SubjectTypeRepository subjectTypeRepository) {
    this.institutionRepository = institutionRepository;
    this.subjectTypeRepository = subjectTypeRepository;
  }

  public CreateSubjectTypeResponse createSubjectType(
      CreateSubjectTypeRequest createSubjectTypeRequest, String institutionIdOrSlug) {

    Institution institution = findInstitution(institutionIdOrSlug);

    requireNameAvailability(institution, createSubjectTypeRequest.name());

    SubjectType newSubjectType =
        subjectTypeRepository.save(
            SubjectType.builder()
                .institution(institution)
                .name(createSubjectTypeRequest.name())
                .build());
    return CreateSubjectTypeResponse.fromSubjectType(newSubjectType);
  }

  @Transactional(readOnly = true)
  public PageResponse<SubjectTypeResponse> listSubjectTypes(
      String institutionIdOrSlug, @Nullable String search, Pageable pageable) {
    Institution institution = findInstitution(institutionIdOrSlug);

    Specification<SubjectType> specification =
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

    Page<SubjectTypeResponse> page =
        subjectTypeRepository
            .findAll(specification, sanitize(pageable))
            .map(SubjectTypeResponse::from);

    return PageResponse.from(page);
  }

  public SubjectTypeResponse show(String institutionIdOrSlug, String subjectTypeIdOrName) {
    Institution institution = findInstitution(institutionIdOrSlug);
    SubjectType subjectType = findOrThrow(institution, subjectTypeIdOrName);

    return SubjectTypeResponse.from(subjectType);
  }

  public SubjectTypeResponse updateName(
      UpdateSubjectTypeNameRequest updateSubjectTypeNameRequest,
      String institutionIdOrSlug,
      String subjectTypeIdOrName) {

    Institution institution = findInstitution(institutionIdOrSlug);
    SubjectType subjectType = findOrThrow(institution, subjectTypeIdOrName);

    requireNameAvailability(institution, updateSubjectTypeNameRequest.name());

    subjectType.setName(updateSubjectTypeNameRequest.name());
    subjectTypeRepository.save(subjectType);

    return SubjectTypeResponse.from(subjectType);
  }

  public SubjectTypeArchiveResponse archiveSubjectType(
      String institutionIdOrSlug, String subjectTypeIdOrName) {

    Institution institution = findInstitution(institutionIdOrSlug);
    SubjectType subjectType = findOrThrow(institution, subjectTypeIdOrName);

    if (subjectType.isArchive()) {
      throw new BadRequestException("Subject Type archived already");
    }

    subjectType.setArchive(true);
    subjectTypeRepository.save(subjectType);

    return SubjectTypeArchiveResponse.from(subjectType);
  }

  public SubjectTypeArchiveResponse unarchiveSubjectType(
      String institutionIdOrSlug, String subjectTypeIdOrName) {

    Institution institution = findInstitution(institutionIdOrSlug);
    SubjectType subjectType = findOrThrow(institution, subjectTypeIdOrName);

    if (!subjectType.isArchive()) {
      throw new BadRequestException("Subject Type unarchived already");
    }

    subjectType.setArchive(false);
    subjectTypeRepository.save(subjectType);

    return SubjectTypeArchiveResponse.from(subjectType);
  }

  @Transactional(readOnly = true)
  public PageResponse<SubjectTypeResponse> listArchivedSubjectTypes(
      String institutionIdOrSlug, @Nullable String search, Pageable pageable) {
    Institution institution = findInstitution(institutionIdOrSlug);

    Specification<SubjectType> specification =
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

    Page<SubjectTypeResponse> page =
        subjectTypeRepository
            .findAll(specification, sanitize(pageable))
            .map(SubjectTypeResponse::from);

    return PageResponse.from(page);
  }

  // Private Methods

  private Institution findInstitution(String institution) {
    return institutionRepository
        .findByIdOrSlug(institution, institution)
        .orElseThrow(() -> new ResourceNotFoundException("Institution Id not found"));
  }

  private void requireNameAvailability(Institution institution, String name) {
    SubjectType subjectType = find(institution, name);
    if (subjectType != null) throw new ConflictException("Subject Type exists already");
  }

  private @Nullable SubjectType find(Institution institution, String subjectIdOrName) {
    return subjectTypeRepository
        .findByIdOrNameInInstitution(subjectIdOrName, subjectIdOrName, institution)
        .orElse(null);
  }

  private SubjectType findOrThrow(Institution institution, String subjectTypeIdOrName) {

    return subjectTypeRepository
        .findByIdOrNameInInstitution(subjectTypeIdOrName, subjectTypeIdOrName, institution)
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
