package com.instello.backend.common.entity.dto;

import com.instello.backend.common.entity.Address;

public record AddressResponse(
        String lineOne,
        String lineTwo,
        String city,
        String state,
        String country,
        String postalCode
) {

    public static AddressResponse from(Address address) {
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
