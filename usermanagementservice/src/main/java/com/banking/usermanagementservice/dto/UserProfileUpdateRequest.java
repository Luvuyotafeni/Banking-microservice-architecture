package com.banking.usermanagementservice.dto;

import com.banking.usermanagementservice.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;


    @Size(min = 2, max = 50, message = "Last name must between 2 and 50 characters")
    private String lastName;

    private Gender gender;

    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    private String country;

    @Valid
    private AddressDto address;
}
