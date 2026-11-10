package in.instello.module.institution.dto;

import in.instello.common.address.dto.AddressResponse;
import in.instello.module.institution.entity.Institution;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record InstitutionResponse(
        String id,
        String name,
        @Nullable String logo,
        String code,
        Instant createdAt,
        @Nullable AddressResponse address
) {

    public static InstitutionResponse from(Institution institution){
        AddressResponse addressResponse = AddressResponse.from(institution.getAddress());

        return  new InstitutionResponse(
                institution.getId(),
                institution.getName(),
                institution.getLogo(),
                institution.getCode(),
                institution.getCreatedAt(),
                addressResponse
        );
    }
}
