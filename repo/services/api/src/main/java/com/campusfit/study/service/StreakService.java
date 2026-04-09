package com.campusfit.study.service;

import com.campusfit.study.entity.Streak;
import com.campusfit.study.repository.StreakRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class StreakService {

    private final StreakRepository streakRepository;

    public StreakService(StreakRepository streakRepository) {
        this.streakRepository = streakRepository;
    }

    @Transactional
    public void updateStreak(Long userId, Long planId, LocalDate completionDate) {
        Streak streak = streakRepository.findByUserIdAndPlanId(userId, planId)
                .orElseGet(() -> Streak.builder()
                        .userId(userId)
                        .planId(planId)
                        .currentStreak(0)
                        .longestStreak(0)
                        .build());

        LocalDate lastActive = streak.getLastActiveDate();

        if (lastActive == null) {
            // First ever completion
            streak.setCurrentStreak(1);
        } else if (completionDate.equals(lastActive)) {
            // Already recorded for today, no change
            return;
        } else if (completionDate.equals(lastActive.plusDays(1))) {
            // Consecutive day
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            // Streak broken
            streak.setCurrentStreak(1);
        }

        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }

        streak.setLastActiveDate(completionDate);
        streakRepository.save(streak);
    }

    @Transactional(readOnly = true)
    public Streak getStreak(Long userId, Long planId) {
        return streakRepository.findByUserIdAndPlanId(userId, planId)
                .orElse(Streak.builder()
                        .userId(userId)
                        .planId(planId)
                        .currentStreak(0)
                        .longestStreak(0)
                        .build());
    }
}
