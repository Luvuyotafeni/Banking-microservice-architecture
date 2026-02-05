package com.banking.usermanagementservice.service.serviceImpl;

import com.banking.usermanagementservice.dto.UserProfileUpdateRequest;
import com.banking.usermanagementservice.dto.UserResponse;
import com.banking.usermanagementservice.entity.Address;
import com.banking.usermanagementservice.entity.User;
import com.banking.usermanagementservice.exception.ResourceNotFoundException;
import com.banking.usermanagementservice.mapper.UserMapper;
import com.banking.usermanagementservice.repository.UserRepository;
import com.banking.usermanagementservice.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfole(UUID userId) {
        log.info("Fetching profile for user: {}", userId);

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        return userMapper.toUserResponse(user);
    }


    @Override
    @Transactional
    public UserResponse updateUserProfile(UUID userId, UserProfileUpdateRequest request) {
        log.info("Updating profile for user: {}", userId);

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(()-> new ResourceNotFoundException("user not found"));

        if (request.getFirstName() != null){
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null){
            user.setLastName(request.getLastName());
        }

        if (request.getGender() != null){
            user.setGender(request.getGender());
        }

        if (request.getCountry() != null){
            user.setCountry(request.getCountry());
        }

        // Update address if provided
        if (request.getAddress() != null) {
            if (user.getAddress() == null) {
                user.setAddress(new Address());
            }

            Address address = user.getAddress();
            if (request.getAddress().getStreetAddress() != null) {
                address.setStreetAddress(request.getAddress().getStreetAddress());
            }
            if (request.getAddress().getStreetAddress2() != null) {
                address.setStreetAddress2(request.getAddress().getStreetAddress2());
            }
            if (request.getAddress().getSuburb() != null) {
                address.setSuburb(request.getAddress().getSuburb());
            }
            if (request.getAddress().getCity() != null) {
                address.setCity(request.getAddress().getCity());
            }
            if (request.getAddress().getProvince() != null) {
                address.setProvince(request.getAddress().getProvince());
            }
            if (request.getAddress().getPostalCode() != null) {
                address.setPostalCode(request.getAddress().getPostalCode());
            }
            if (request.getAddress().getCountry() != null) {
                address.setCountry(request.getAddress().getCountry());
            }
        }

        User updatedUser = userRepository.save(user);
        log.info("profile updated successfully for user: {}", userId);

        return userMapper.toUserResponse(updatedUser);
    }
}
