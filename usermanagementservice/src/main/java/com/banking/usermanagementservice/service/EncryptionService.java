package com.banking.usermanagementservice.service;

public interface EncryptionService {

    /**
     * Encrypt sensitive data (like ID numbers)
     * @param data Data to encrypt
     * @return Encrypted data
     */
    String encrypt(String data);

    /**
     * Decrypt sensitive data
     * @param encryptedData Encrypted data
     * @return Decrypted data
     */
    String decrypt(String encryptedData);
}
