package com.banking.usermanagementservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressResponse {

    private UUID id;
    private String streetAddress;
    private String streetAddress2;
    private String suburb;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    private String formattedAddress;
}
