package in.instello.module.institution.dto;

import in.instello.common.address.dto.AddressRequest;
import org.jspecify.annotations.Nullable;
import jakarta.validation.constraints.NotBlank;

public record UpdateInstitutionDetailsRequest(
   @Nullable @NotBlank String name,
   @Nullable @NotBlank AddressRequest address
) {}
