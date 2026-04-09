package com.campusfit.shared.encryption;

import org.springframework.stereotype.Component;

@Component
public class FieldMasker {

    public String mask(String value) {
        if (value == null) {
            return null;
        }
        int length = value.length();
        if (length <= 4) {
            return "***";
        }
        String first = value.substring(0, 2);
        String last = value.substring(length - 2);
        return first + "***" + last;
    }

    public String maskNumeric(Number value) {
        if (value == null) {
            return null;
        }
        return "***";
    }

    public String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***@" + (atIndex >= 0 ? email.substring(atIndex + 1) : "***");
        }
        return email.charAt(0) + "***@" + email.substring(atIndex + 1);
    }
}
