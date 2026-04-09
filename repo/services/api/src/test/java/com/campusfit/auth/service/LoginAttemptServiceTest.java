package com.campusfit.auth.service;

import com.campusfit.auth.entity.User;
import com.campusfit.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private UserRepository userRepository;

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService(userRepository, 5, 15);
    }

    @Test
    void recordFailedAttempt_incrementsCounter() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .failedAttempts(2)
                .status(User.UserStatus.ACTIVE)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        loginAttemptService.recordFailedAttempt(user);

        assertThat(user.getFailedAttempts()).isEqualTo(3);
        assertThat(user.getLockoutUntil()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    void recordFailedAttempt_locksAfterMaxAttempts() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .failedAttempts(4)
                .status(User.UserStatus.ACTIVE)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        LocalDateTime before = LocalDateTime.now().plusMinutes(14);
        loginAttemptService.recordFailedAttempt(user);
        LocalDateTime after = LocalDateTime.now().plusMinutes(16);

        assertThat(user.getFailedAttempts()).isEqualTo(5);
        assertThat(user.getLockoutUntil()).isNotNull();
        assertThat(user.getLockoutUntil()).isAfter(before);
        assertThat(user.getLockoutUntil()).isBefore(after);
        assertThat(user.getStatus()).isEqualTo(User.UserStatus.LOCKED);
        verify(userRepository).save(user);
    }

    @Test
    void isLocked_returnsTrueWhenLocked() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .failedAttempts(5)
                .lockoutUntil(LocalDateTime.now().plusMinutes(10))
                .status(User.UserStatus.LOCKED)
                .build();

        boolean locked = loginAttemptService.isLocked(user);

        assertThat(locked).isTrue();
    }

    @Test
    void isLocked_returnsFalseAfterLockoutExpires() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .failedAttempts(5)
                .lockoutUntil(LocalDateTime.now().minusMinutes(1))
                .status(User.UserStatus.LOCKED)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        boolean locked = loginAttemptService.isLocked(user);

        assertThat(locked).isFalse();
        assertThat(user.getFailedAttempts()).isEqualTo(0);
        assertThat(user.getLockoutUntil()).isNull();
        assertThat(user.getStatus()).isEqualTo(User.UserStatus.ACTIVE);
    }

    @Test
    void resetAttempts_clearsCounterAndLockout() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .failedAttempts(5)
                .lockoutUntil(LocalDateTime.now().plusMinutes(10))
                .status(User.UserStatus.LOCKED)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        loginAttemptService.resetAttempts(user);

        assertThat(user.getFailedAttempts()).isEqualTo(0);
        assertThat(user.getLockoutUntil()).isNull();
        assertThat(user.getStatus()).isEqualTo(User.UserStatus.ACTIVE);
        verify(userRepository).save(user);
    }
}
