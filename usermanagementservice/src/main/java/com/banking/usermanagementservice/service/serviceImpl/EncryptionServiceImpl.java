package com.banking.usermanagementservice.service.serviceImpl;

import com.banking.usermanagementservice.service.EncryptionService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Service
@Slf4j
public class EncryptionServiceImpl implements EncryptionService {

    @Value("${app.encryption.secret:MySecretEncryptionLey123!@#}")
    private String secretKey;

    private SecretKey key;
    private static final String ALGORITHM = "AES";

    @PostConstruct
    public void init() {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            keyBytes = Arrays.copyOf(keyBytes, 16);
            key = new SecretKeySpec(keyBytes, ALGORITHM);
            log.info("Encryption service initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize encryption service", e);
            throw new RuntimeException("Failed to initialize encryption", e);

        }
    }

    @Override
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e){
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed");
        }
    }

    @Override
    public String decrypt(String encryptedData) {
        try{
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e){
            log.error("Decryption dailed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
