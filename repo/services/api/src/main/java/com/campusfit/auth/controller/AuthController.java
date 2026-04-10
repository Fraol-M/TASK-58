package com.campusfit.auth.controller;

import com.campusfit.auth.dto.LoginRequest;
import com.campusfit.auth.dto.LoginResponse;
import com.campusfit.auth.dto.SignUpRequest;
import com.campusfit.auth.dto.UserDto;
import com.campusfit.auth.service.AuthService;
import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.security.SecurityContextHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/sign-up")
    public ResponseEntity<ApiResponse<UserDto>> signUp(@Valid @RequestBody SignUpRequest request) {
        UserDto user = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(user));
    }

    @PostMapping("/auth/sign-in")
    public ResponseEntity<ApiResponse<LoginResponse>> signIn(@Valid @RequestBody LoginRequest request,
                                                              HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        if (!StringUtils.hasText(userAgent)) {
            userAgent = "unknown";
        }

        LoginResponse response = authService.signIn(request, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // Sign-out is intentionally idempotent: it succeeds even when no token is present.
    // This avoids client-side 401 errors on double-logout or expired-session scenarios.
    // Token invalidation is only attempted when a valid Bearer token is supplied.
    @PostMapping("/auth/sign-out")
    public ResponseEntity<ApiResponse<Void>> signOut(HttpServletRequest request) {
        String token = extractToken(request);
        if (StringUtils.hasText(token)) {
            authService.signOut(token);
        }
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        Long userId = SecurityContextHelper.getCurrentUserId();
        UserDto user = authService.getCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users/{userId}/roles")
    public ResponseEntity<ApiResponse<UserDto>> assignRole(@PathVariable Long userId,
                                                            @RequestParam String roleCode) {
        UserDto user = authService.assignRole(userId, roleCode);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
