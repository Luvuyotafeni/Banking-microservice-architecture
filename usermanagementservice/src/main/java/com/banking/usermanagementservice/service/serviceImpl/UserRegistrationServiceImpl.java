package com.banking.usermanagementservice.service.serviceImpl;

import com.banking.usermanagementservice.dto.UserRegistrationRequest;
import com.banking.usermanagementservice.dto.UserResponse;
import com.banking.usermanagementservice.entity.Address;
import com.banking.usermanagementservice.entity.Role;
import com.banking.usermanagementservice.entity.User;
import com.banking.usermanagementservice.entity.UserCredentials;
import com.banking.usermanagementservice.enums.ApprovalStatus;
import com.banking.usermanagementservice.enums.RoleType;
import com.banking.usermanagementservice.exception.DuplicateResourceException;
import com.banking.usermanagementservice.exception.ResourceNotFoundException;
import com.banking.usermanagementservice.mapper.UserMapper;
import com.banking.usermanagementservice.repository.RoleRepository;
import com.banking.usermanagementservice.repository.UserCredentialsRepository;
import com.banking.usermanagementservice.repository.UserRepository;
import com.banking.usermanagementservice.service.EncryptionService;
import com.banking.usermanagementservice.service.UserRegistrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserCredentialsRepository credentialsRepository; // Added for credentials persistence
    private final PasswordEncoder passwordEncoder; // Added for hashing the placeholder
    private final EncryptionService encryptionService;
    private final UserMapper userMapper;

    @Value("${app.security.password.expiration-days:90}")
    private int passwordExpirationDays;

    @Override
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Starting user registration for email {}", request.getEmail());

        // 1. Validation for duplicate email
        if (emailExists(request.getEmail())){
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new DuplicateResourceException("Email already registered");
        }

        // 2. Validation for duplicate ID
        if (idNumberExists(request.getIdNumber())){
            log.warn("Registration failed: ID Number already exists");
            throw new DuplicateResourceException("Id number already registered");
        }

        // 3. Fetch Role
        Role customerRole = roleRepository.findByName(RoleType.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Customer role not found"));

        // 4. Map Address
        Address address = Address.builder()
                .streetAddress(request.getAddress().getStreetAddress())
                .streetAddress2(request.getAddress().getStreetAddress2())
                .suburb(request.getAddress().getSuburb())
                .city(request.getAddress().getCity())
                .province(request.getAddress().getProvince())
                .postalCode(request.getAddress().getPostalCode())
                .country(request.getAddress().getCountry())
                .build();

        // 5. Encrypt sensitive ID
        String encryptedIdNumber = encryptionService.encrypt(request.getIdNumber());

        // 6. Build User Entity
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

        user.addRole(customerRole);

        // 7. Save User
        User savedUser = userRepository.save(user);

        // 8. Generate and Save Credentials (Placeholder Password)
        createPlaceholderCredentials(savedUser, request.getIdNumber());

        log.info("User registered successfully with ID: {} and default credentials", savedUser.getId());
        return userMapper.toUserResponse(savedUser);
    }

    private void createPlaceholderCredentials(User user, String rawIdNumber) {
        // Logic: firstName (lowercase) + gender + last 4 digits of ID
        // Example: johnMALE9087
        String placeholder = user.getFirstName().toLowerCase() +
                user.getGender().name() +
                rawIdNumber.substring(rawIdNumber.length() - 4);

        UserCredentials credentials = UserCredentials.builder()
                .userId(user.getId())
                .passwordHash(passwordEncoder.encode(placeholder))
                .passwordCreatedAt(LocalDateTime.now())
                .passwordExpiresAt(LocalDateTime.now().plusDays(passwordExpirationDays))
                .isFirstLogin(true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .build();

        credentialsRepository.save(credentials);
        log.debug("Placeholder credentials created for userId: {}", user.getId());
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.toLowerCase());
    }

    @Override
    public boolean idNumberExists(String idNumber) {
        String encryptedIdNumber = encryptionService.encrypt(idNumber);
        return userRepository.existsByIdNumber(encryptedIdNumber);
    }
}