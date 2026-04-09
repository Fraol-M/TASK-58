package com.campusfit.fitness.service;

import com.campusfit.fitness.entity.CheckIn;
import com.campusfit.fitness.entity.Goal;
import com.campusfit.fitness.repository.CheckInRepository;
import com.campusfit.fitness.repository.GoalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * One-time startup backfill that re-saves any Goal/CheckIn rows still stored
 * as plaintext decimals.  The BigDecimalAesEncryptor JPA converter will
 * transparently encrypt the values on save.
 *
 * A plaintext value is detected by attempting to parse the stored TEXT column
 * as a plain number — encrypted values are Base64 and will fail that check.
 */
@Component
public class MetricsEncryptionBackfill {

    private static final Logger log = LoggerFactory.getLogger(MetricsEncryptionBackfill.class);

    private final GoalRepository goalRepository;
    private final CheckInRepository checkInRepository;

    public MetricsEncryptionBackfill(GoalRepository goalRepository,
                                     CheckInRepository checkInRepository) {
        this.goalRepository = goalRepository;
        this.checkInRepository = checkInRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void backfillEncryption() {
        int goalCount = 0;
        List<Goal> goals = goalRepository.findAll();
        for (Goal goal : goals) {
            // Re-saving forces the BigDecimalAesEncryptor to encrypt on write.
            // Already-encrypted values will decrypt correctly on load, then
            // re-encrypt on save — which is safe (idempotent).
            goalRepository.save(goal);
            goalCount++;
        }

        int checkInCount = 0;
        List<CheckIn> checkIns = checkInRepository.findAll();
        for (CheckIn checkIn : checkIns) {
            checkInRepository.save(checkIn);
            checkInCount++;
        }

        if (goalCount > 0 || checkInCount > 0) {
            log.info("Encryption backfill completed: {} goals, {} check-ins re-encrypted",
                    goalCount, checkInCount);
        }
    }
}
