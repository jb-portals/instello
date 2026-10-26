package com.instello.backend.accessRequest.dto;

import com.instello.backend.common.entity.Address;

public record AddressResponse(
        String addressLine,
        String city,
        String state,
        String country
) {

    public static AddressResponse from(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressResponse(address.getAddressLine(), address.getCity(), address.getState(), address.getCountry());
    }
}
