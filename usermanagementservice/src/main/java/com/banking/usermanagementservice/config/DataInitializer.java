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
import com.banking.usermanagementservice.service.EmailService;
import com.banking.usermanagementservice.service.OtpService;
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
    private final OtpService otpService;
    private final EmailService emailService;

    @Value("${app.super-admin.email}")
    private String superAdminEmail;

    @Value("${app.super-admin.first-name}")
    private String superAdminFirstName;

    @Value("${app.super-admin.last-name}")
    private String superAdminLastName;

    @Value("${app.super-admin.id-number}")
    private String superAdminIdNumber;

    @Value("${app.super-admin.initial-password}")
    private String superAdminPassword;

    @Value("${app.security.password.expiration-days}")
    private int passwordExpirationDays;

    @Value("${app.super-admin.send-otp-email:false}")
    private boolean sendOtpEmail;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data initialization...");

        // Initialize roles
        initializeRoles();

        // Initialize super admin
        initializeSuperAdmin();

        log.info("Data initialization completed successfully");
    }

    private void initializeRoles() {
        log.info("Initializing roles...");

        // Create SUPER_ADMIN role if not exists
        if (!roleRepository.existsByName(RoleType.SUPER_ADMIN)) {
            Role superAdminRole = Role.builder()
                    .name(RoleType.SUPER_ADMIN)
                    .description(RoleType.SUPER_ADMIN.getDescription())
                    .build();
            roleRepository.save(superAdminRole);
            log.info("Created SUPER_ADMIN role");
        }

        // Create CUSTOMER role if not exists
        if (!roleRepository.existsByName(RoleType.CUSTOMER)) {
            Role customerRole = Role.builder()
                    .name(RoleType.CUSTOMER)
                    .description(RoleType.CUSTOMER.getDescription())
                    .build();
            roleRepository.save(customerRole);
            log.info("Created CUSTOMER role");
        }

        log.info("Roles initialization completed");
    }

    private void initializeSuperAdmin() {
        log.info("Checking for super admin...");

        // Check if super admin already exists
        if (userRepository.existsByEmail(superAdminEmail)) {
            log.info("Super admin already exists");
            return;
        }

        try {
            // Get super admin role
            Role superAdminRole = roleRepository.findByName(RoleType.SUPER_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Super Admin role not found"));

            // Encrypt ID number
            String encryptedIdNumber = encryptionService.encrypt(superAdminIdNumber);

            // Create super admin user
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

            // Add super admin role
            superAdmin.addRole(superAdminRole);

            // Save super admin user
            User savedSuperAdmin = userRepository.save(superAdmin);
            log.info("Super admin user created with ID: {}", savedSuperAdmin.getId());

            // Create credentials for super admin
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

            // ✅ Generate OTP for super admin
            String otp = otpService.generateOtp(savedSuperAdmin.getId());
            log.info("OTP generated for super admin");

            // ✅ Send email with OTP (optional, controlled by config)
            if (sendOtpEmail) {
                try {
                    emailService.sendApprovalEmail(
                            savedSuperAdmin.getEmail(),
                            savedSuperAdmin.getFullName(),
                            otp
                    );
                    log.info("OTP email sent to super admin");
                } catch (Exception e) {
                    log.warn("Failed to send OTP email to super admin: {}", e.getMessage());
                    // Don't fail the initialization if email fails
                }
            }

            log.info("===========================================");
            log.info("SUPER ADMIN CREATED SUCCESSFULLY");
            log.info("Email: {}", superAdminEmail);
            log.info("Initial Password: {}", superAdminPassword);
            log.info("One-Time Password (OTP): {}", otp);
            log.info("OTP Expires in: 15 minutes");
            log.info("-------------------------------------------");
            log.info("FIRST LOGIN OPTIONS:");
            log.info("1. Use OTP login: POST /auth/login/otp");
            log.info("   { \"email\": \"" + superAdminEmail + "\", \"otp\": \"" + otp + "\" }");
            log.info("2. Or use password: POST /auth/login");
            log.info("   { \"email\": \"" + superAdminEmail + "\", \"password\": \"" + superAdminPassword + "\" }");
            log.info("-------------------------------------------");
            log.info("PLEASE CHANGE PASSWORD IMMEDIATELY!");
            log.info("===========================================");

        } catch (Exception e) {
            log.error("Failed to create super admin", e);
            throw new RuntimeException("Super admin initialization failed", e);
        }
    }
}