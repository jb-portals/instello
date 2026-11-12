package in.instello.module.circular.service;

import in.instello.module.circular.dto.CreateCircularRequest;
import in.instello.module.circular.entity.Circular;
import in.instello.module.circular.repository.CircularRepository;
import in.instello.module.user.entity.User;
import in.instello.module.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CircularService {

  private final CircularRepository circularRepository;
  private final UserRepository userRepository;

  // Create a circular
  public Circular createCircular(CreateCircularRequest request, String email) {

    User user =
        userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

    Circular circular =
        Circular.builder()
            .title(request.title())
            .content(request.content())
            .expiresAt(request.expiresAt())
            .createdBy(user)
            .build();

    return circularRepository.save(circular);
  }

  // Get all circulars
  @Transactional(readOnly = true)
  public List<Circular> getAllCirculars() {
    return circularRepository.findAll();
  }

  // Get circular by ID
  @Transactional(readOnly = true)
  public Circular getCircularById(String id) {
    return circularRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Circular not found: " + id));
  }

  // Update a circular
  public Circular updateCircular(String id, Circular updatedCircular) {

    Circular existingCircular = getCircularById(id);

    existingCircular.setTitle(updatedCircular.getTitle());
    existingCircular.setContent(updatedCircular.getContent());

    return circularRepository.save(existingCircular);
  }

  // Delete a circular
  public void deleteCircular(String id) {
    Circular circular = getCircularById(id);
    circularRepository.delete(circular);
  }
}
