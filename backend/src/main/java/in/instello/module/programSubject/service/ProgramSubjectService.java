package in.instello.module.programSubject.service;

import in.instello.common.exceptions.ConflictException;
import in.instello.common.exceptions.ResourceNotFoundException;
import in.instello.module.institution.entity.Institution;
import in.instello.module.institution.repository.InstitutionRepository;
import in.instello.module.programSubject.entity.SubjectType;
import in.instello.module.programSubject.repository.ProgramSubjectRepository;
import in.instello.module.programSubject.repository.SubjectTypeRepository;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ProgramSubjectService {

  private final InstitutionRepository institutionRepository;
  private final ProgramSubjectRepository programSubjectRepository;
  private final SubjectTypeRepository subjectTypeRepository;

  private static final Set<String> SORTABLE_FIELDS = Set.of("name", "code", "alias");
  private static final int MAX_PAGE_SIZE = 100;

  public ProgramSubjectService(
      InstitutionRepository institutionRepository,
      ProgramSubjectRepository programSubjectRepository,
      SubjectTypeRepository subjectTypeRepository) {
    this.institutionRepository = institutionRepository;
    this.programSubjectRepository = programSubjectRepository;
    this.subjectTypeRepository = subjectTypeRepository;
  }

  /*
  public AddProgramSubjectResponse add(
      AddProgramSubjectRequest addProgramSubjectRequest, String institutionIdOrSlug, String programIdOrAlias, String academicStageId ) {

    Institution institution = findInstitution(institutionIdOrSlug);

    Program program=programService.findOrThrow(institutionIdOrSlug, programIdOrAlias);


    ProgramSubject newProgramSubject =
    programSubjectRepository.save(
          ProgramSubject.builder()
                .institution(institution)
                .program(program)
                .subject(subject)
                .academicStage(academicStage)
                .subjectType(subjectType)
                .description(addProgramSubjectRequest.description())
                .build());
    return addSubjectResponse.fromSubject(newProgramSubject);
  }
   */

  // Private Methods

  private Institution findInstitution(String institution) {
    return institutionRepository
        .findByIdOrSlug(institution, institution)
        .orElseThrow(() -> new ResourceNotFoundException("Institution Id not found"));
  }

  private String findNameAvailabilityOrThrow(Institution institution, String name) {
    SubjectType subjectType = find(institution, name);
    if (subjectType != null) throw new ConflictException("Subject Type exists already");
    return name;
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
