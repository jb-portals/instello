package com.instello.backend.common.entity.dto;

import com.instello.backend.common.entity.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 255) String lineOne,
        @Size(max = 255) String lineTwo,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String state,

        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "Postal code must contain exactly 6 digits")
        String postalCode
) {
        public Address toAddress(){
                return Address.builder()
                        .lineOne(lineOne().trim())
                        .lineTwo(lineTwo().trim())
                        .city(city().trim())
                        .country(Address.DEFAULT_COUNTRY)
                        .state(state().trim())
                        .postalCode(postalCode().trim())
                        .build();
        }
}
