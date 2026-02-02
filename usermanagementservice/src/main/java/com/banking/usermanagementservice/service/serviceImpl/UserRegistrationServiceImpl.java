package com.banking.usermanagementservice.service.serviceImpl;

import com.banking.usermanagementservice.dto.UserRegistrationRequest;
import com.banking.usermanagementservice.dto.UserResponse;
import com.banking.usermanagementservice.entity.Address;
import com.banking.usermanagementservice.entity.Role;
import com.banking.usermanagementservice.entity.User;
import com.banking.usermanagementservice.enums.ApprovalStatus;
import com.banking.usermanagementservice.enums.RoleType;
import com.banking.usermanagementservice.exception.DuplicateResourceException;
import com.banking.usermanagementservice.exception.ResourceNotFoundException;
import com.banking.usermanagementservice.repository.RoleRepository;
import com.banking.usermanagementservice.repository.UserRepository;
import com.banking.usermanagementservice.service.EncryptionService;
import com.banking.usermanagementservice.service.UserRegistrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EncryptionService encryptionService;


    @Override
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Starting user registration for email {}", request.getEmail());

        //checking for duplicate email
        if (emailExists(request.getEmail())){
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new DuplicateResourceException("Email already registered");
        }

        //checking for duplicate id
        if (emailExists(request.getIdNumber())){
            log.warn("Registration failed: ID Number already exists");
            throw new DuplicateResourceException("Id number already registered");
        }

        //Get Customer Role
        Role customerRole = roleRepository.findByName(RoleType.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Customer role not found"));

        //build address
        Address address = Address.builder()
                .streetAddress(request.getAddress().getStreetAddress())
                .streetAddress2(request.getAddress().getStreetAddress2())
                .suburb(request.getAddress().getSuburb())
                .city(request.getAddress().getCity())
                .province(request.getAddress().getProvince())
                .postalCode(request.getAddress().getPostalCode())
                .country(request.getAddress().getCountry())
                .build();

        //Encrypt sensitive data(ID number)
        String encryptedIdNumber = encryptionService.encrypt(request.getIdNumber());

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase())
                .idNumber(encryptedIdNumber)
                .gender(request.getGender())
                .country(request.getCountry())
                .address(address)
                .approvalStatus(ApprovalStatus.PENDING)
                .isActive(false)
                .isEmailVerified(false)
                .isDeleted(false)
                .build();

        //Add customer role
        user.addRole(customerRole);

        //Save User
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public boolean emailExists(String email) {
        return false;
    }

    @Override
    public boolean idNumberExists(String idNumber) {
        return false;
    }
}
