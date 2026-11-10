package in.instello.module.institution.dto;

import in.instello.common.address.dto.AddressRequest;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

public record UpdateInstitutionDetailsRequest(
    @Nullable @NotBlank String name, @Nullable @NotBlank AddressRequest address) {}
