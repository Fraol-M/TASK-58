package com.campusfit.shared.config;

import com.campusfit.shared.encryption.EncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptionConfig {

    private static final Logger log = LoggerFactory.getLogger(EncryptionConfig.class);

    @Value("${app.encryption-key}")
    private String encryptionKey;

    private static final String KNOWN_PLACEHOLDER_KEY = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Bean
    public EncryptionService encryptionService() {
        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw new IllegalStateException("app.encryption-key must be configured");
        }
        if (!encryptionKey.matches("[0-9a-fA-F]+")) {
            throw new IllegalStateException("app.encryption-key must be a hex string");
        }
        if (encryptionKey.length() != 64) {
            throw new IllegalStateException(
                    "app.encryption-key must be exactly 64 hex characters (32 bytes / 256 bits) for AES-256. Got: "
                            + encryptionKey.length() + " characters");
        }
        if (KNOWN_PLACEHOLDER_KEY.equals(encryptionKey)) {
            throw new IllegalStateException(
                    "app.encryption-key is set to the default placeholder value. "
                            + "Generate a deployment-specific key: openssl rand -hex 32");
        }
        log.info("AES-256 encryption configured with valid 256-bit key");
        return new EncryptionService(encryptionKey);
    }
}
