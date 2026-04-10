package com.campusfit.auth.service;

import com.campusfit.auth.entity.User;
import com.campusfit.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    private final UserRepository userRepository;
    private final int maxAttempts;
    private final int lockoutDurationMinutes;

    public LoginAttemptService(UserRepository userRepository,
                               @Value("${app.lockout.max-attempts:5}") int maxAttempts,
                               @Value("${app.lockout.duration-minutes:15}") int lockoutDurationMinutes) {
        this.userRepository = userRepository;
        this.maxAttempts = maxAttempts;
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(User user) {
        int newAttempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(newAttempts);

        if (newAttempts >= maxAttempts) {
            user.setLockoutUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
            user.setStatus(User.UserStatus.LOCKED);
        }

        userRepository.save(user);
    }

    public boolean isLocked(User user) {
        if (user.getLockoutUntil() == null) {
            return false;
        }
        if (user.getLockoutUntil().isAfter(LocalDateTime.now())) {
            return true;
        }
        // Lockout has expired, reset
        resetAttempts(user);
        return false;
    }

    @Transactional
    public void resetAttempts(User user) {
        user.setFailedAttempts(0);
        user.setLockoutUntil(null);
        if (user.getStatus() == User.UserStatus.LOCKED) {
            user.setStatus(User.UserStatus.ACTIVE);
        }
        userRepository.save(user);
    }
}
