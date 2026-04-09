package com.campusfit.auth.service;

import com.campusfit.auth.dto.LoginRequest;
import com.campusfit.auth.dto.LoginResponse;
import com.campusfit.auth.dto.SignUpRequest;
import com.campusfit.auth.dto.UserDto;
import com.campusfit.auth.entity.Role;
import com.campusfit.auth.entity.Session;
import com.campusfit.auth.entity.User;
import com.campusfit.auth.entity.UserRole;
import com.campusfit.auth.mapper.AuthMapper;
import com.campusfit.auth.repository.RoleRepository;
import com.campusfit.auth.repository.SessionRepository;
import com.campusfit.auth.repository.UserRepository;
import com.campusfit.auth.repository.UserRoleRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private PasswordService passwordService;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private AuthMapper authMapper;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, roleRepository, userRoleRepository,
                sessionRepository, passwordService, loginAttemptService, authMapper, 30);
    }

    @Test
    void signUp_createsUserWithHashedPassword() {
        SignUpRequest request = SignUpRequest.builder()
                .username("newuser")
                .password("plaintext123")
                .email("new@example.com")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordService.encode("plaintext123")).thenReturn("hashed_password");

        User savedUser = User.builder().id(1L).username("newuser")
                .passwordHash("hashed_password").status(User.UserStatus.ACTIVE).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Role role = Role.builder().id(10L).code("REGULAR_USER").build();
        when(roleRepository.findByCode("REGULAR_USER")).thenReturn(Optional.of(role));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(UserRole.builder().build());

        UserDto expectedDto = UserDto.builder().id(1L).username("newuser")
                .roles(List.of("REGULAR_USER")).status("ACTIVE").build();
        when(authMapper.toUserDto(any(User.class), anyList())).thenReturn(expectedDto);

        UserDto result = authService.signUp(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("hashed_password");
        assertThat(result.getUsername()).isEqualTo("newuser");
    }

    @Test
    void signUp_alwaysAssignsRegularUserRole() {
        SignUpRequest request = SignUpRequest.builder()
                .username("newuser")
                .password("plaintext123")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordService.encode(anyString())).thenReturn("hashed");

        User savedUser = User.builder().id(1L).username("newuser").status(User.UserStatus.ACTIVE).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Role role = Role.builder().id(10L).code("REGULAR_USER").build();
        when(roleRepository.findByCode("REGULAR_USER")).thenReturn(Optional.of(role));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(UserRole.builder().build());
        when(authMapper.toUserDto(any(User.class), anyList())).thenReturn(UserDto.builder().build());

        authService.signUp(request);

        verify(roleRepository).findByCode("REGULAR_USER");
        verify(roleRepository, never()).findByCode("ADMIN");
        ArgumentCaptor<UserRole> userRoleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(userRoleCaptor.capture());
        assertThat(userRoleCaptor.getValue().getRoleId()).isEqualTo(10L);
    }

    @Test
    void signIn_returnsTokenOnSuccess() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser").password("password123").build();

        User user = User.builder().id(1L).username("testuser")
                .passwordHash("hashed").status(User.UserStatus.ACTIVE).failedAttempts(0).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(loginAttemptService.isLocked(user)).thenReturn(false);
        when(passwordService.matches("password123", "hashed")).thenReturn(true);
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRoleRepository.findByUserId(1L)).thenReturn(List.of(
                UserRole.builder().userId(1L).roleId(10L).build()));
        when(roleRepository.findById(10L)).thenReturn(Optional.of(
                Role.builder().id(10L).code("REGULAR_USER").build()));
        when(authMapper.toUserDto(any(User.class), anyList())).thenReturn(
                UserDto.builder().id(1L).username("testuser").build());

        LoginResponse result = authService.signIn(request, "127.0.0.1", "Mozilla/5.0");

        assertThat(result.getToken()).isNotNull();
        assertThat(result.getToken()).isNotEmpty();
        assertThat(result.getExpiresAt()).isAfter(LocalDateTime.now());
        verify(loginAttemptService).resetAttempts(user);
    }

    @Test
    void signIn_throwsOnWrongPassword() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser").password("wrong").build();

        User user = User.builder().id(1L).username("testuser")
                .passwordHash("hashed").status(User.UserStatus.ACTIVE).failedAttempts(0).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(loginAttemptService.isLocked(user)).thenReturn(false);
        when(passwordService.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.signIn(request, "127.0.0.1", "Mozilla"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void signIn_throwsWhenLocked() {
        LoginRequest request = LoginRequest.builder()
                .username("lockeduser").password("password123").build();

        User user = User.builder().id(1L).username("lockeduser")
                .status(User.UserStatus.ACTIVE).failedAttempts(5).build();

        when(userRepository.findByUsername("lockeduser")).thenReturn(Optional.of(user));
        when(loginAttemptService.isLocked(user)).thenReturn(true);

        assertThatThrownBy(() -> authService.signIn(request, "127.0.0.1", "Mozilla"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Account is locked");
    }

    @Test
    void signIn_recordsFailedAttempt() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser").password("wrong").build();

        User user = User.builder().id(1L).username("testuser")
                .passwordHash("hashed").status(User.UserStatus.ACTIVE).failedAttempts(0).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(loginAttemptService.isLocked(user)).thenReturn(false);
        when(passwordService.matches("wrong", "hashed")).thenReturn(false);

        try {
            authService.signIn(request, "127.0.0.1", "Mozilla");
        } catch (BusinessException ignored) {
        }

        verify(loginAttemptService).recordFailedAttempt(user);
    }

    @Test
    void validateSession_returnsUserPrincipal() {
        Session session = Session.builder()
                .id(1L).userId(1L).token("valid-token")
                .expiresAt(LocalDateTime.now().plusMinutes(15)).build();

        User user = User.builder().id(1L).username("testuser")
                .status(User.UserStatus.ACTIVE).build();

        when(sessionRepository.findByToken("valid-token")).thenReturn(Optional.of(session));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserId(1L)).thenReturn(List.of(
                UserRole.builder().userId(1L).roleId(10L).build()));
        when(roleRepository.findById(10L)).thenReturn(Optional.of(
                Role.builder().id(10L).code("REGULAR_USER").build()));

        UserPrincipal principal = authService.validateSession("valid-token");

        assertThat(principal).isNotNull();
        assertThat(principal.getId()).isEqualTo(1L);
        assertThat(principal.getUsername()).isEqualTo("testuser");
        assertThat(principal.getRoles()).contains("REGULAR_USER");
    }

    @Test
    void validateSession_throwsOnExpiredSession() {
        Session session = Session.builder()
                .id(1L).userId(1L).token("expired-token")
                .expiresAt(LocalDateTime.now().minusMinutes(5)).build();

        when(sessionRepository.findByToken("expired-token")).thenReturn(Optional.of(session));

        UserPrincipal principal = authService.validateSession("expired-token");

        assertThat(principal).isNull();
    }
}
