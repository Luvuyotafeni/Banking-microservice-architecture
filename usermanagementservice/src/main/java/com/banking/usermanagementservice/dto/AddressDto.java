package com.banking.usermanagementservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    @NotBlank(message = "Street address is required")
    @Size(min = 5, max = 200, message = "Street address must be between 5 and 200 characters")
    private String streetAddress;

    @Size(max = 200, message = "Street address 2 must not exceed 200 characters")
    private String streetAddress2;

    @NotBlank(message = "Suburb is required")
    @Size(min = 2, max = 100, message = "Suburb must be between 2 and 100 characters")
    private String suburb;

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    private String city;

    @NotBlank(message = "Province is required")
    @Size(min = 2, max = 100, message = "Province must be between 2 and 100 characters")
    private String province;

    @NotBlank(message = "Postal code is required")
    @Size(min = 4, max = 10, message = "Postal code must be between 4 and 10 characters")
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    private String country;
}
