package in.instello.module.academicPattern.service;

import in.instello.common.exceptions.ResourceNotFoundException;
import in.instello.module.academicPattern.dto.AcademicStageResponse;
import in.instello.module.academicPattern.entity.AcademicPattern;
import in.instello.module.academicPattern.entity.AcademicStage;
import in.instello.module.academicPattern.entity.AcademicSystemType;
import in.instello.module.academicPattern.repository.AcademicStageRepository;
import in.instello.module.institution.entity.Institution;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
class AcademicStageService {

  private final AcademicStageRepository repository;

  AcademicStageService(AcademicStageRepository repository) {
    this.repository = repository;
  }

  List<AcademicStage> generate(AcademicPattern pattern) {
    AcademicSystemType systemType = pattern.getSystemType();
    int stagesPerYear = systemType == AcademicSystemType.SEMESTER ? 2 : 1;
    List<AcademicStage> stages =
        IntStream.rangeClosed(1, pattern.getDurationInYears() * stagesPerYear)
            .mapToObj(
                sequence ->
                    AcademicStage.builder()
                        .academicPattern(pattern)
                        .name(name(systemType, sequence))
                        .alias(alias(systemType, sequence))
                        .sequenceNumber(sequence)
                        .yearNumber((sequence - 1) / stagesPerYear + 1)
                        .build())
            .toList();
    return repository.saveAllAndFlush(stages);
  }

  List<AcademicStageResponse> list(AcademicPattern pattern) {
    return responses(
        pattern, repository.findByAcademicPattern_IdOrderBySequenceNumberAsc(pattern.getId()));
  }

  List<AcademicStageResponse> responses(AcademicPattern pattern, List<AcademicStage> stages) {
    AcademicSystemType systemType = pattern.getSystemType();
    return stages.stream()
        .map(
            stage ->
                new AcademicStageResponse(
                    stage.getId(),
                    name(systemType, stage.getSequenceNumber()),
                    alias(systemType, stage.getSequenceNumber()),
                    stage.getDescription(),
                    stage.getSequenceNumber(),
                    stage.getYearNumber(),
                    stage.getCreatedAt(),
                    stage.getUpdatedAt()))
        .toList();
  }

  void deleteAll(String patternId) {
    repository.deleteAllForPattern(patternId);
  }

  /**
   * Get {@link AcademicStage} in {@link Institution}
   *
   * @param institution {@link Institution} object
   * @param academicStageIdOrAlias {@link String} id or alias of academic stage
   * @throws ResourceNotFoundException Academic stage not found
   */
  public AcademicStage getInInstitutionOrThrow(
      Institution institution, String academicStageIdOrAlias) {
    return repository
        .findByIdOrAliasInInstitution(institution, academicStageIdOrAlias)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Academic stage not found: " + academicStageIdOrAlias));
  }

  private static String name(AcademicSystemType type, int sequence) {
    return (type == AcademicSystemType.SEMESTER ? "Sem " : "Year ") + sequence;
  }

  private static String alias(AcademicSystemType type, int sequence) {
    return (type == AcademicSystemType.SEMESTER ? "s" : "y") + sequence;
  }
}
