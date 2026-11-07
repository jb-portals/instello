package com.instello.backend.common.entity.dto;

import com.instello.backend.common.entity.Address;
import org.jspecify.annotations.Nullable;

public record AddressResponse(
        String lineOne,
        @Nullable String lineTwo,
        String city,
        String state,
        String country,
        String postalCode
) {

    public static @Nullable AddressResponse from(@Nullable Address address) {
        if (address == null) {
            return null;
        }
        return new AddressResponse(
                address.getLineOne(),
                address.getLineTwo(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getPostalCode());
    }
}
