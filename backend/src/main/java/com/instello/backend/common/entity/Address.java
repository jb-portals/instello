package com.instello.backend.common.entity;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Check;
import org.jspecify.annotations.Nullable;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(check = {@CheckConstraint(constraint = "postal_code ~ '^[0-9]{6}$'")})
public class Address {

    public static final String DEFAULT_COUNTRY = "India";

    @Column(nullable = false)
    private String lineOne;

    private @Nullable String lineTwo;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 6)
    private String postalCode;

    @Builder.Default
    @Column(nullable = false)
    private String country = DEFAULT_COUNTRY;
}
