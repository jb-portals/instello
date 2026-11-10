package in.instello.module.institution.dto;

import in.instello.module.institution.entity.Institution;

public record CreateInstitutionResponse(String id, String name, String slug) {
  public static CreateInstitutionResponse fromInstitution(Institution institution) {
    return new CreateInstitutionResponse(
        institution.getId(), institution.getName(), institution.getSlug());
  }
}
