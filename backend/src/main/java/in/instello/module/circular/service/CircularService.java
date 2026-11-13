package in.instello.module.circular.service;

import in.instello.common.exception.app.ResourceNotFoundException;
import in.instello.module.circular.dto.CreateCircularRequest;
import in.instello.module.circular.entity.Circular;
import in.instello.module.circular.repository.CircularRepository;
import in.instello.module.institution.entity.Institution;
import in.instello.module.institution.repository.InstitutionRepository;
import in.instello.module.organization.entity.Organization;
import in.instello.module.organization.repository.OrganizationRepository;
import in.instello.module.user.entity.User;
import in.instello.module.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import in.instello.module.circular.entity.CircularStatus;
import java.time.Instant;
import in.instello.module.circular.dto.ScheduleCircularRequest;
import in.instello.module.circular.entity.CircularStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional
public class CircularService {

  private final CircularRepository circularRepository;
  private final UserRepository userRepository;
  private final InstitutionRepository institutionRepository;
  private final OrganizationRepository organizationRepository;

  public Circular createCircular(
      CreateCircularRequest request, String email, String organizationId, String institutionId) {

    // Find organization
    Organization organization =
        organizationRepository
            .findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

    // Find institution belonging to that organization
    Institution institution =
        institutionRepository
            .findInstitutionByOrganizationAndId(organization, institutionId)
            .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

    // Find logged-in user
    User user =
        userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // Create circular
    Circular circular =
        Circular.builder()
            .title(request.title())
            .content(request.content())
            .expiresAt(request.expiresAt())
            .institution(institution)
            .createdBy(user)
            .build();

    return circularRepository.save(circular);
  }

  // Get all circulars
  @Transactional(readOnly = true)
public List<Circular> getAllCirculars(
    String organizationId,
    String institutionId) {

    Organization organization = organizationRepository
        .findById(organizationId)
        .orElseThrow(() ->
            new ResourceNotFoundException("Organization not found"));

    Institution institution = institutionRepository
        .findInstitutionByOrganizationAndId(organization, institutionId)
        .orElseThrow(() ->
            new ResourceNotFoundException("Institution not found"));

    return circularRepository.findByInstitution(institution);
}
// Search, filter and paginate circulars
@Transactional(readOnly = true)
public Page<Circular> searchAndFilterCirculars(
    String organizationId,
    String institutionId,
    String search,
    CircularStatus status,
    Pageable pageable) {

    Organization organization = organizationRepository
        .findById(organizationId)
        .orElseThrow(() ->
            new ResourceNotFoundException("Organization not found"));

    Institution institution = institutionRepository
        .findInstitutionByOrganizationAndId(organization, institutionId)
        .orElseThrow(() ->
            new ResourceNotFoundException("Institution not found"));

    return circularRepository.searchAndFilterCirculars(
        institution, search, status, pageable);
}
  // Get circular by ID
  @Transactional(readOnly = true)
  public Circular getCircularById(
      String organizationId,
      String institutionId,
      String id) {
  
      Organization organization = organizationRepository
          .findById(organizationId)
          .orElseThrow(() ->
              new ResourceNotFoundException("Organization not found"));
  
      Institution institution = institutionRepository
          .findInstitutionByOrganizationAndId(organization, institutionId)
          .orElseThrow(() ->
              new ResourceNotFoundException("Institution not found"));
  
      return circularRepository
          .findByIdAndInstitution(id, institution)
          .orElseThrow(() ->
              new ResourceNotFoundException("Circular not found"));
  }

  // Update a circular
  public Circular updateCircular(
    String organizationId,
    String institutionId,
    String id,
    Circular updatedCircular) {

    Circular existingCircular =
        getCircularById(organizationId, institutionId, id);

    existingCircular.setTitle(updatedCircular.getTitle());
    existingCircular.setContent(updatedCircular.getContent());

    return circularRepository.save(existingCircular);
}

// Publish a circular
public Circular publishCircular(
    String organizationId,
    String institutionId,
    String id) {

    Circular circular =
        getCircularById(organizationId, institutionId, id);

    circular.setStatus(CircularStatus.PUBLISHED);
    circular.setPublishedAt(Instant.now());
    circular.setScheduledAt(null);

    return circularRepository.save(circular);
}

// Schedule a circular
public Circular scheduleCircular(
    String organizationId,
    String institutionId,
    String id,
    Instant scheduledAt) {

    Circular circular =
        getCircularById(organizationId, institutionId, id);

    circular.setStatus(CircularStatus.SCHEDULED);
    circular.setScheduledAt(scheduledAt);
    circular.setPublishedAt(null);

    return circularRepository.save(circular);
}

  // Delete a circular
  public void deleteCircular(
    String organizationId,
    String institutionId,
    String id) {

    Circular circular =
        getCircularById(organizationId, institutionId, id);

    circularRepository.delete(circular);
}}
