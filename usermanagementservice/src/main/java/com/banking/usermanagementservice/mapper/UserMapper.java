package com.banking.usermanagementservice.mapper;

import com.banking.usermanagementservice.dto.AddressResponse;
import com.banking.usermanagementservice.dto.UserResponse;
import com.banking.usermanagementservice.entity.Address;
import com.banking.usermanagementservice.entity.Role;
import com.banking.usermanagementservice.entity.User;
import jdk.jfr.Name;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "fullName", source = ".", qualifiedByName = "getFullName")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRolesToStrings")
    @Mapping(target = "address", source = "address")
    UserResponse toUserResponse(User user);

    @Mapping(target = "formattedAddress", source = ".", qualifiedByName = "getFormattedAddress")
    AddressResponse toAddressResponse(Address address);

    @Named("getFullName")
    default String getFullName(User user){
        return user.getFirstName() + " "+ user.getLastName();
    }

    @Named("mapRolesToStrings")
    default Set<String> mapRolesToStrings(Set<Role> roles){
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }

    @Named("getFormattedAddress")
    default String getFormattedAddress(Address address){
        if (address == null) return  null;
        return address.getFormattedAddress();
    }
}
