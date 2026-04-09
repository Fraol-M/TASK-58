package com.campusfit.masterdata.policy;

import com.campusfit.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class EffectiveDatePolicy {

    public void validate(LocalDate effectiveFrom, LocalDate effectiveTo) {
        if (effectiveFrom == null) {
            throw new BusinessException("Effective from date is required");
        }

        if (effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new BusinessException("Effective to date must be after effective from date");
        }
    }
}
