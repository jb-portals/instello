package in.instello.module.institution.dto;

import in.instello.common.address.dto.AddressRequest;
import in.instello.module.institution.entity.Institution;
import jakarta.validation.constraints.NotNull;

public record CreateInstitutionRequest(
    @NotNull String name,
    @NotNull String code,
    @NotNull String slug,
    @NotNull AddressRequest address) {

  public Institution toInstitution() {
    return Institution.builder()
        .name(name().trim())
        .code(code().trim())
        .slug(slug().trim())
        .address(address().toAddress())
        .build();
  }
}
