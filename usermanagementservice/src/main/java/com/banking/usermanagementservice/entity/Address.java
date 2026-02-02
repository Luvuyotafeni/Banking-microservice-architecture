package com.banking.usermanagementservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String streetAddress;

    @Column
    private String streetAddress2;

    @Column(nullable = false)
    private String suburb;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String country;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    //Helper method for formatted address
    public String getFormattedAddress(){
        StringBuilder formatted = new StringBuilder();
        formatted.append(streetAddress);

        if (streetAddress2 != null && !streetAddress2.isEmpty()){
            formatted.append(", ").append(streetAddress2);
        }

        formatted.append(", ").append(suburb)
                .append(", ").append(city)
                .append(", ").append(province)
                .append(", ").append(postalCode)
                .append(", ").append(country);

        return formatted.toString();
    }
}
