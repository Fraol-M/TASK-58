package com.campusfit.auth.service;

import com.campusfit.auth.dto.*;
import com.campusfit.auth.entity.*;
import com.campusfit.auth.mapper.AuthMapper;
import com.campusfit.auth.repository.*;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import com.campusfit.shared.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final SessionRepository sessionRepository;
    private final PasswordService passwordService;
    private final LoginAttemptService loginAttemptService;
    private final AuthMapper authMapper;
    private final int sessionTimeoutMinutes;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository,
                       SessionRepository sessionRepository,
                       PasswordService passwordService,
                       LoginAttemptService loginAttemptService,
                       AuthMapper authMapper,
                       @Value("${app.session.timeout-minutes:30}") int sessionTimeoutMinutes) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.sessionRepository = sessionRepository;
        this.passwordService = passwordService;
        this.loginAttemptService = loginAttemptService;
        this.authMapper = authMapper;
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }

    @Transactional
    public UserDto signUp(SignUpRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists: " + request.getUsername());
        }

        User user = User.builder()
            .username(request.getUsername())
            .passwordHash(passwordService.encode(request.getPassword()))
            .email(request.getEmail())
            .phone(request.getPhone())
            .status(User.UserStatus.ACTIVE)
            .failedAttempts(0)
            .build();

        user = userRepository.save(user);

        Role role = roleRepository.findByCode("REGULAR_USER")
            .orElseThrow(() -> new BusinessException("Role not found: REGULAR_USER"));

        UserRole userRole = UserRole.builder()
            .userId(user.getId())
            .roleId(role.getId())
            .assignedAt(LocalDateTime.now())
            .build();
        userRoleRepository.save(userRole);

        List<String> roles = List.of(role.getCode());
        return authMapper.toUserDto(user, roles);
    }

    @Transactional
    public LoginResponse signIn(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BusinessException("Invalid username or password"));

        if (user.getStatus() == User.UserStatus.DISABLED || user.getStatus() == User.UserStatus.DELETED) {
            throw new BusinessException("Account is not active");
        }

        if (loginAttemptService.isLocked(user)) {
            throw new BusinessException("Account is locked due to too many failed attempts. Please try again later.");
        }

        if (!passwordService.matches(request.getPassword(), user.getPasswordHash())) {
            loginAttemptService.recordFailedAttempt(user);
            throw new BusinessException("Invalid username or password");
        }

        loginAttemptService.resetAttempts(user);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(sessionTimeoutMinutes);

        Session session = Session.builder()
            .userId(user.getId())
            .token(token)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .expiresAt(expiresAt)
            .lastAccessedAt(LocalDateTime.now())
            .build();
        sessionRepository.save(session);

        List<String> roles = getUserRoleCodes(user.getId());
        UserDto userDto = authMapper.toUserDto(user, roles);

        return LoginResponse.builder()
            .token(token)
            .user(userDto)
            .expiresAt(expiresAt)
            .build();
    }

    @Transactional
    public void signOut(String token) {
        Session session = sessionRepository.findByToken(token)
            .orElseThrow(() -> new BusinessException("Invalid session token"));
        sessionRepository.delete(session);
    }

    @Transactional(readOnly = true)
    public UserPrincipal validateSession(String token) {
        Session session = sessionRepository.findByToken(token)
            .orElse(null);

        if (session == null) {
            return null;
        }

        LocalDateTime lastActivity = session.getLastAccessedAt() != null
            ? session.getLastAccessedAt() : session.getCreatedAt();
        if (lastActivity == null || lastActivity.plusMinutes(sessionTimeoutMinutes).isBefore(LocalDateTime.now())) {
            return null;
        }

        User user = userRepository.findById(session.getUserId())
            .orElse(null);

        if (user == null) {
            return null;
        }

        Set<String> roles = getUserRoleCodes(user.getId()).stream()
            .collect(Collectors.toSet());

        return new UserPrincipal(user.getId(), user.getUsername(), roles);
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<String> roles = getUserRoleCodes(userId);
        return authMapper.toUserDto(user, roles);
    }

    @Transactional
    public UserDto assignRole(Long userId, String roleCode) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Role role = roleRepository.findByCode(roleCode)
            .orElseThrow(() -> new BusinessException("Role not found: " + roleCode));

        boolean alreadyAssigned = userRoleRepository.findByUserId(userId).stream()
            .anyMatch(ur -> ur.getRoleId().equals(role.getId()));

        if (!alreadyAssigned) {
            UserRole userRole = UserRole.builder()
                .userId(userId)
                .roleId(role.getId())
                .assignedAt(LocalDateTime.now())
                .build();
            userRoleRepository.save(userRole);
        }

        List<String> roles = getUserRoleCodes(userId);
        return authMapper.toUserDto(user, roles);
    }

    private List<String> getUserRoleCodes(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        return userRoles.stream()
            .map(ur -> roleRepository.findById(ur.getRoleId())
                .map(Role::getCode)
                .orElse(null))
            .filter(code -> code != null)
            .collect(Collectors.toList());
    }
}
