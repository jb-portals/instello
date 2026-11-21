package in.instello.module.faculty;

import in.instello.common.address.dto.AddressRequest;
import in.instello.common.exception.app.BadRequestException;
import in.instello.common.exception.app.ConflictException;
import in.instello.common.exception.app.ResourceNotFoundException;
import in.instello.common.exception.response.PageResponse;
import in.instello.module.faculty.dto.ActivateFacultyResponse;
import in.instello.module.faculty.dto.CheckEmailRequest;
import in.instello.module.faculty.dto.CheckPrimaryPhoneNumberRequest;
import in.instello.module.faculty.dto.CheckStaffIdRequest;
import in.instello.module.faculty.dto.CreateFacultyRequest;
import in.instello.module.faculty.dto.CreateFacultyResponse;
import in.instello.module.faculty.dto.FacultyAvailabilityResponse;
import in.instello.module.faculty.dto.FacultyResponse;
import in.instello.module.faculty.dto.UpdateFacultyRequest;
import in.instello.module.faculty.dto.UpdateFacultyStaffIdRequest;
import in.instello.module.faculty.dto.UpdateFacultyStaffIdResponse;
import in.instello.module.institution.entity.Institution;
import in.instello.module.institution.repository.InstitutionRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacultyService {

  private final InstitutionRepository institutionRepository;
  private final FacultyRepository facultyRepository;

  private static final Set<String> SORTABLE_FIELDS = Set.of("name", "code", "alias");
  private static final int MAX_PAGE_SIZE = 100;

  public FacultyService(
      InstitutionRepository institutionRepository, FacultyRepository facultyRepository) {
    this.institutionRepository = institutionRepository;
    this.facultyRepository = facultyRepository;
  }

  public CreateFacultyResponse create(
      CreateFacultyRequest createFacultyRequest, String institutionIdOrSlug) {

    Institution institution = findInstitution(institutionIdOrSlug);

    String staffId =
        findStaffIdAvailabilityOrThrow(institutionIdOrSlug, createFacultyRequest.staffId());

    @Nullable Faculty faculty = findEmailAvailability(institution, createFacultyRequest.email());
    if (faculty != null) throw new ConflictException("Email exists already");

    @Nullable Faculty faculty1 =
        findPrimaryPhoneNumberAvailability(institution, createFacultyRequest.primaryPhoneNumber());
    if (faculty1 != null) throw new ConflictException("Primary Phone Number exists already");

    Faculty newFaculty =
        facultyRepository.save(
            Faculty.builder()
                .institution(institution)
                .firstName(createFacultyRequest.firstName())
                .lastName(createFacultyRequest.lastName())
                .email(createFacultyRequest.email())
                .staffId(staffId)
                .dateOfBirth(createFacultyRequest.dateOfBirth())
                .profilePhoto(createFacultyRequest.profilePhoto())
                .designation(createFacultyRequest.designation())
                .qualification(createFacultyRequest.qualification())
                .specialization(createFacultyRequest.specialization())
                .joinedDate(createFacultyRequest.joinedDate())
                .primaryPhoneNumber(createFacultyRequest.primaryPhoneNumber())
                .secondaryPhoneNumber(createFacultyRequest.SecondaryPhoneNumber())
                .address(createFacultyRequest.address().toAddress())
                .build());

    return CreateFacultyResponse.fromFaculty(newFaculty);
  }

  @Transactional(readOnly = true)
  public PageResponse<FacultyResponse> list(
      String institutionIdOrSlug, @Nullable String search, Pageable pageable) {
    Institution institution = findInstitution(institutionIdOrSlug);

    Specification<Faculty> specification =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          predicates.add(
              cb.and(
                  cb.equal(root.get("institution"), institution), cb.isTrue(root.get("active"))));

          String term = trimToNull(search);
          if (term != null) {
            String pattern = "%" + term.toLowerCase(Locale.ROOT) + "%";
            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("staffId")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("primaryPhoneNumber")), pattern)));
          }
          return cb.and(predicates.toArray(Predicate[]::new));
        };

    Page<FacultyResponse> page =
        facultyRepository.findAll(specification, sanitize(pageable)).map(FacultyResponse::from);

    return PageResponse.from(page);
  }

  @Transactional(readOnly = true)
  public FacultyResponse show(String institutionIdOrSlug, String id) {
    Institution institution = findInstitution(institutionIdOrSlug);
    return facultyRepository
        .findByIdAndInstitution(id, institution)
        .map(FacultyResponse::from)
        .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
  }

  @Transactional
  public FacultyResponse update(
      String institutionIdOrSlug, String staffId, UpdateFacultyRequest request) {
    Institution institution = findInstitution(institutionIdOrSlug);
    Faculty faculty =
        facultyRepository
            .findByIdOrStaffIdInInstitution(staffId, institution)
            .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));

    String firstName = request.getFirstName();
    if (firstName != null) {
      faculty.setFirstName(firstName);
    }
    if (request.isLastNameProvided()) {
      faculty.setLastName(request.getLastName());
    }
    LocalDate dateOfBirth = request.getDateOfBirth();
    if (dateOfBirth != null) {
      faculty.setDateOfBirth(dateOfBirth);
    }
    if (request.isProfilePhotoProvided()) {
      faculty.setProfilePhoto(request.getProfilePhoto());
    }
    String designation = request.getDesignation();
    if (designation != null) {
      faculty.setDesignation(designation);
    }
    if (request.isQualificationProvided()) {
      faculty.setQualification(request.getQualification());
    }
    if (request.isSpecializationProvided()) {
      faculty.setSpecialization(request.getSpecialization());
    }
    if (request.isJoiningDateProvided()) {
      faculty.setJoinedDate(request.getJoiningDate());
    }
    if (request.isSecondaryPhoneNumberProvided()) {
      faculty.setSecondaryPhoneNumber(request.getSecondaryPhoneNumber());
    }
    AddressRequest address = request.getAddress();
    if (address != null) {
      faculty.setAddress(address.toAddress());
    }
    return FacultyResponse.from(faculty);
  }

  @Transactional
  public UpdateFacultyStaffIdResponse updateStaffId(
      String institutionIdOrSlug, String facultyId, UpdateFacultyStaffIdRequest request) {
    Institution institution = findInstitution(institutionIdOrSlug);
    Faculty faculty =
        facultyRepository
            .findByIdAndInstitution(facultyId, institution)
            .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));

    String staffId = request.staffId();
    if (!staffId.equals(faculty.getStaffId())) {
      if (facultyRepository.findByStaffIdAndInstitution(staffId, institution).isPresent()) {
        throw new ConflictException("StaffId exists already");
      }
      faculty.setStaffId(staffId);
      try {
        facultyRepository.flush();
      } catch (DataIntegrityViolationException exception) {
        if (exception.getCause() instanceof ConstraintViolationException violation
            && "uk_faculty_institution_staffid".equalsIgnoreCase(violation.getConstraintName())) {
          throw new ConflictException("StaffId exists already");
        }
        throw exception;
      }
    }
    return new UpdateFacultyStaffIdResponse(
        faculty.getId(), faculty.getFirstName(), faculty.getLastName(), faculty.getStaffId());
  }

  public FacultyAvailabilityResponse checkStaffId(
      String institution, CheckStaffIdRequest checkStaffIdRequest) {
    String staffId = findStaffIdAvailabilityOrThrow(institution, checkStaffIdRequest.staffId());
    return new FacultyAvailabilityResponse(staffId != null ? true : false);
  }

  public FacultyAvailabilityResponse checkEmail(
      String institutionIdOrSlug, CheckEmailRequest checkEmailRequest) {
    Institution institution = findInstitution(institutionIdOrSlug);
    @Nullable Faculty faculty = findEmailAvailability(institution, checkEmailRequest.email());
    if (faculty != null) throw new ConflictException("Email exists already");

    return new FacultyAvailabilityResponse(faculty != null ? false : true);
  }

  public FacultyAvailabilityResponse checkPrimaryPhoneNumber(
      String institutionIdOrSlug, CheckPrimaryPhoneNumberRequest checkPrimaryPhoneNumberRequest) {
    Institution institution = findInstitution(institutionIdOrSlug);
    @Nullable Faculty faculty =
        findPrimaryPhoneNumberAvailability(
            institution, checkPrimaryPhoneNumberRequest.primaryPhoneNumber());
    if (faculty != null) throw new ConflictException("Primary Phone Number exists already");

    return new FacultyAvailabilityResponse(faculty != null ? false : true);
  }

  public ActivateFacultyResponse deactivate(String institutionIdOrSlug, String staffId) {

    Faculty faculty = find(institutionIdOrSlug, staffId);

    if (!faculty.isActive()) {
      throw new BadRequestException("Faculty Deactivated already");
    }

    faculty.setActive(false);
    faculty.setInactivatedAt(Instant.now());
    facultyRepository.save(faculty);

    return ActivateFacultyResponse.from(faculty);
  }

  @Transactional(readOnly = true)
  public PageResponse<FacultyResponse> listInactive(
      String institutionIdOrSlug, @Nullable String search, Pageable pageable) {
    Institution institution = findInstitution(institutionIdOrSlug);

    Specification<Faculty> specification =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          predicates.add(
              cb.and(
                  cb.equal(root.get("institution"), institution), cb.isFalse(root.get("active"))));

          String term = trimToNull(search);
          if (term != null) {
            String pattern = "%" + term.toLowerCase(Locale.ROOT) + "%";
            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("staffId")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("primaryPhoneNumber")), pattern)));
          }
          return cb.and(predicates.toArray(Predicate[]::new));
        };

    Page<FacultyResponse> page =
        facultyRepository.findAll(specification, sanitize(pageable)).map(FacultyResponse::from);

    return PageResponse.from(page);
  }

  public ActivateFacultyResponse activate(String institutionIdOrSlug, String staffId) {

    Faculty faculty = find(institutionIdOrSlug, staffId);

    if (faculty.isActive()) {
      throw new BadRequestException("Faculty Activated Already");
    }

    faculty.setActive(true);
    facultyRepository.save(faculty);

    return ActivateFacultyResponse.from(faculty);
  }

  // private methods starts here

  private Institution findInstitution(String institution) {
    return institutionRepository
        .findByIdOrSlug(institution, institution)
        .orElseThrow(() -> new ResourceNotFoundException("InstitutionId not found"));
  }

  private @Nullable Faculty find(String institutionIdOrSlug, String staffId) {
    Institution institution = findInstitution(institutionIdOrSlug);
    return facultyRepository
        .findByIdOrStaffIdAndInstitution(staffId, staffId, institution)
        .orElse(null);
  }

  private String findStaffIdAvailabilityOrThrow(String institution, String staffId) {
    Faculty faculty = find(institution, staffId);
    if (faculty != null) throw new ConflictException("StaffId exists already");
    return staffId;
  }

  private @Nullable Faculty findEmailAvailability(Institution institution, String email) {
    return facultyRepository.findByEmailAndInstitution(email, institution).orElse(null);
  }

  private @Nullable Faculty findPrimaryPhoneNumberAvailability(
      Institution institution, String primaryPhoneNumber) {
    return facultyRepository
        .findByPrimaryPhoneNumberAndInstitution(primaryPhoneNumber, institution)
        .orElse(null);
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
