package com.campusfit.shared.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

@Converter
public class BigDecimalAesEncryptor implements AttributeConverter<BigDecimal, String> {

    @Override
    public String convertToDatabaseColumn(BigDecimal attribute) {
        if (attribute == null) {
            return null;
        }
        EncryptionService encryptionService = getEncryptionService();
        if (encryptionService == null) {
            return attribute.toPlainString();
        }
        return encryptionService.encrypt(attribute.toPlainString());
    }

    @Override
    public BigDecimal convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        // Try parsing as a plain decimal first — legacy rows are stored as
        // unencrypted numeric strings like "200.00".  Encrypted values are
        // Base64 and will always fail this parse.
        try {
            return new BigDecimal(dbData);
        } catch (NumberFormatException ignored) {
            // Not a plain number — fall through to decryption
        }

        EncryptionService encryptionService = getEncryptionService();
        if (encryptionService == null) {
            throw new IllegalStateException(
                    "Cannot decrypt value: EncryptionService is not available");
        }
        String decrypted = encryptionService.decrypt(dbData);
        return new BigDecimal(decrypted);
    }

    private EncryptionService getEncryptionService() {
        return ApplicationContextProvider.getBean(EncryptionService.class);
    }
}
