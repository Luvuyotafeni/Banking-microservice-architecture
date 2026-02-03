package com.banking.usermanagementservice.config;

import com.banking.usermanagementservice.entity.Role;
import com.banking.usermanagementservice.entity.User;
import com.banking.usermanagementservice.entity.UserCredentials;
import com.banking.usermanagementservice.enums.ApprovalStatus;
import com.banking.usermanagementservice.enums.Gender;
import com.banking.usermanagementservice.enums.RoleType;
import com.banking.usermanagementservice.repository.RoleRepository;
import com.banking.usermanagementservice.repository.UserCredentialsRepository;
import com.banking.usermanagementservice.repository.UserRepository;
import com.banking.usermanagementservice.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserCredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;


    @Value("${app.super-admin.email}")
    private String  superAdminEmail;

    @Value("${app.super-admin.first-name}")
    private String  superAdminFirstName;

    @Value("${app.super-admin.last-name}")
    private String  superAdminLastName;

    @Value("${app.super-admin.id-number}")
    private String  superAdminIdNumber;

    @Value("${app.super-admin.initial-password}")
    private String  superAdminPassword;

    @Value("${app.security.password.expiration-days}")
    private int passwordExpirationDays;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");

        //initialize roles
        initializeRoles();

    }

    private void initializeRoles(){
        log.info("Initializing roles...");

        if (!roleRepository.existsByName(RoleType.SUPER_ADMIN)){
            Role superAdminRole = Role.builder()
                    .name(RoleType.SUPER_ADMIN)
                    .description(RoleType.SUPER_ADMIN.getDescription())
                    .build();
            roleRepository.save(superAdminRole);
            log.info("Created SUPER_ADMIN role");
        }

        if (!roleRepository.existsByName(RoleType.CUSTOMER)){
            Role customerRole = Role.builder()
                    .name(RoleType.CUSTOMER)
                    .description(RoleType.CUSTOMER.getDescription())
                    .build();
            roleRepository.save(customerRole);
            log.info("Created CUSTOMER role");
        }
        log.info("Roles initialization completed");
    }

    private void intializeSuperAdmin(){
        log.info("Checking for super admin...");

        if (userRepository.existsByEmail(superAdminEmail)){
            log.info("Super admin already exists");
            return;
        }

        try {
            Role superAdminRole = roleRepository.findByName(RoleType.SUPER_ADMIN)
                    .orElseThrow(()-> new RuntimeException("Super Admin role not found"));

            //encrypt id number
            String encryptedIdNumber = encryptionService.encrypt(superAdminIdNumber);

            User superAdmin = User.builder()
                    .firstName(superAdminFirstName)
                    .lastName(superAdminLastName)
                    .email(superAdminEmail.toLowerCase())
                    .idNumber(encryptedIdNumber)
                    .gender(Gender.PREFER_NO_TO_SAY)
                    .country("South Africa")
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .isActive(true)
                    .isEmailVerified(true)
                    .isDeleted(false)
                    .build();

            //Add super admin role
            superAdmin.addRole(superAdminRole);

            //save super admin user
            User savedSuperAdmin = userRepository.save(superAdmin);
            log.info("Super admin user created with ID: {}", savedSuperAdmin.getId());

            //Create credentials for super admin
            String hashedPassword = passwordEncoder.encode(superAdminPassword);

            UserCredentials credentials = UserCredentials.builder()
                    .userId(savedSuperAdmin.getId())
                    .passwordHash(hashedPassword)
                    .passwordCreatedAt(LocalDateTime.now())
                    .passwordExpiresAt(LocalDateTime.now().plusDays(passwordExpirationDays))
                    .isFirstLogin(true)
                    .isLocked(false)
                    .failedLoginAttempts(0)
                    .build();

            credentialsRepository.save(credentials);
            log.info("Super admin credentials created");

            log.info("=============================================");
            log.info("SUPER ADMIN CREATED SUCCESSFULLY");
            log.info("Email: {}", superAdminEmail);
            log.info("Initial password: {}", superAdminPassword);
            log.info("PLEASE CHANGE THIS PASSWORD IMMEDIATELY");
            log.info("==============================================");

        } catch (Exception e) {

            log.info("Failed to create super admin", e);
            throw new RuntimeException("Super admin intialization failed", e);
        }

    }
}
