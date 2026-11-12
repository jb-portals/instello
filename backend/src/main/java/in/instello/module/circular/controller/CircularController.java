package in.instello.module.circular.controller;

import in.instello.module.circular.dto.CreateCircularRequest;
import in.instello.module.circular.entity.Circular;
import in.instello.module.circular.service.CircularService;
import in.instello.security.AppUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v0.1/circulars")
@RequiredArgsConstructor
public class CircularController {

  private final CircularService circularService;

  // Create circular
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Circular createCircular(
      @Valid @RequestBody CreateCircularRequest request,
      @AuthenticationPrincipal AppUserDetails userDetails) {

    return circularService.createCircular(request, userDetails.email());
  }

  // Get all circulars
  @GetMapping
  public List<Circular> getAllCirculars() {
    return circularService.getAllCirculars();
  }

  // Get circular by ID
  @GetMapping("/{id}")
  public Circular getCircularById(@PathVariable String id) {
    return circularService.getCircularById(id);
  }

  // Update circular
  @PutMapping("/{id}")
  public Circular updateCircular(@PathVariable String id, @RequestBody Circular circular) {

    return circularService.updateCircular(id, circular);
  }

  // Delete circular
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCircular(@PathVariable String id) {
    circularService.deleteCircular(id);
  }
}
