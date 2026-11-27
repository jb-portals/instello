package in.instello.module.academicPattern.service;

import in.instello.module.academicPattern.entity.AcademicPattern;
import in.instello.module.academicPattern.entity.AcademicSystemType;
import in.instello.module.academicPattern.repository.AcademicPatternRepository;
import in.instello.module.organization.entity.Organization;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademicPatternSeedService {

  private final AcademicPatternRepository repository;
  private final AcademicStageService stages;

  AcademicPatternSeedService(AcademicPatternRepository repository, AcademicStageService stages) {
    this.repository = repository;
    this.stages = stages;
  }

  @Transactional
  public void seedDefaults(Organization organization) {
    seed(organization, "Engineering", "4-year semester-based engineering program", 4);
    seed(organization, "Diploma", "3-year semester-based diploma program", 3);
  }

  private void seed(Organization organization, String name, String description, int duration) {
    if (repository.existsByOrganization_IdAndName(organization.getId(), name)) {
      return;
    }
    AcademicPattern pattern =
        repository.save(
            AcademicPattern.builder()
                .organization(organization)
                .name(name)
                .description(description)
                .durationInYears(duration)
                .systemType(AcademicSystemType.SEMESTER)
                .build());
    stages.generate(pattern);
  }
}
