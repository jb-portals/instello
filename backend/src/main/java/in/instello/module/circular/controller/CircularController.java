package in.instello.module.circular.controller;

import in.instello.module.circular.dto.CreateCircularRequest;
import in.instello.module.circular.dto.ScheduleCircularRequest;
import in.instello.module.circular.entity.Circular;
import in.instello.module.circular.service.CircularService;
import in.instello.security.AppUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PatchMapping;
import in.instello.module.circular.entity.CircularStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/organizations/{organizationId}/institutions/{institutionId}/circulars")
@RequiredArgsConstructor
public class CircularController {

  private final CircularService circularService;

  // Create circular
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Circular createCircular(
      @PathVariable String organizationId,
      @PathVariable String institutionId,
      @Valid @RequestBody CreateCircularRequest request,
      @AuthenticationPrincipal AppUserDetails userDetails) {

    return circularService.createCircular(
        request, userDetails.email(), organizationId, institutionId);
  }

  // Get all circulars
  @GetMapping
  public List<Circular> getAllCirculars(
      @PathVariable String organizationId, @PathVariable String institutionId) {

    return circularService.getAllCirculars(organizationId, institutionId);
  }
// Search, filter, paginate and sort circulars
@GetMapping("/search")
public Page<Circular> searchAndFilterCirculars(
    @PathVariable String organizationId,
    @PathVariable String institutionId,
    @RequestParam(required = false) String search,
    @RequestParam(required = false) CircularStatus status,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "createdAt") String sortBy,
    @RequestParam(defaultValue = "desc") String direction) {

    Sort.Direction sortDirection =
        Sort.Direction.fromString(direction);

    Pageable pageable = PageRequest.of(
        page,
        size,
        Sort.by(sortDirection, sortBy)
    );

    return circularService.searchAndFilterCirculars(
        organizationId,
        institutionId,
        search,
        status,
        pageable
    );
}

  // Get circular by ID
  @GetMapping("/{id}")
  public Circular getCircularById(
      @PathVariable String organizationId,
      @PathVariable String institutionId,
      @PathVariable String id) {

    return circularService.getCircularById(organizationId, institutionId, id);
  }

  // Update circular
  @PutMapping("/{id}")
  public Circular updateCircular(
      @PathVariable String organizationId,
      @PathVariable String institutionId,
      @PathVariable String id,
      @RequestBody Circular circular) {

    return circularService.updateCircular(organizationId, institutionId, id, circular);
  }

  // Publish a circular
  @PatchMapping("/{id}/publish")
  public Circular publishCircular(
      @PathVariable String organizationId,
      @PathVariable String institutionId,
      @PathVariable String id) {

    return circularService.publishCircular(organizationId, institutionId, id);
  }

  // Schedule a circular
  @PatchMapping("/{id}/schedule")
  public Circular scheduleCircular(
      @PathVariable String organizationId,
      @PathVariable String institutionId,
      @PathVariable String id,
      @Valid @RequestBody ScheduleCircularRequest request) {

    return circularService.scheduleCircular(
        organizationId, institutionId, id, request.scheduledAt());
  }

  // Delete circular
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCircular(
      @PathVariable String organizationId,
      @PathVariable String institutionId,
      @PathVariable String id) {

    circularService.deleteCircular(organizationId, institutionId, id);
  }
}