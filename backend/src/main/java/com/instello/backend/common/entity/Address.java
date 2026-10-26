package com.instello.backend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Address {

    public static final String DEFAULT_COUNTRY = "India";

    @Column(nullable = false)
    private String addressLine;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Builder.Default
    @Column(nullable = false)
    private String country = DEFAULT_COUNTRY;
}
