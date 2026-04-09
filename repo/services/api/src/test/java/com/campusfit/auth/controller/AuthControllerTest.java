package com.campusfit.auth.controller;

import com.campusfit.auth.dto.LoginRequest;
import com.campusfit.auth.dto.LoginResponse;
import com.campusfit.auth.dto.SignUpRequest;
import com.campusfit.auth.dto.UserDto;
import com.campusfit.auth.service.AuthService;
import com.campusfit.shared.config.SecurityConfig;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.GlobalExceptionHandler;
import com.campusfit.shared.security.AccessDeniedHandlerImpl;
import com.campusfit.shared.security.AuthenticationEntryPointImpl;
import com.campusfit.shared.security.SessionAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.campusfit.shared.security.UserPrincipal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private SessionAuthenticationFilter sessionAuthenticationFilter;

    @Test
    void signUp_success() throws Exception {
        SignUpRequest request = SignUpRequest.builder()
                .username("newuser")
                .password("password123")
                .email("new@example.com")
                .build();

        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("newuser")
                .email("new@example.com")
                .roles(List.of("REGULAR_USER"))
                .status("ACTIVE")
                .build();

        when(authService.signUp(any(SignUpRequest.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.roles[0]").value("REGULAR_USER"));
    }

    @Test
    void signUp_duplicateUsername() throws Exception {
        SignUpRequest request = SignUpRequest.builder()
                .username("existing")
                .password("password123")
                .build();

        when(authService.signUp(any(SignUpRequest.class)))
                .thenThrow(new BusinessException("Username already exists: existing"));

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists: existing"));
    }

    @Test
    void signUp_invalidInput() throws Exception {
        SignUpRequest request = SignUpRequest.builder()
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.username").value("Username is required"));
    }

    @Test
    void signIn_success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .roles(List.of("REGULAR_USER"))
                .status("ACTIVE")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .token("test-token-uuid")
                .user(userDto)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        when(authService.signIn(any(LoginRequest.class), anyString(), anyString()))
                .thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("test-token-uuid"))
                .andExpect(jsonPath("$.data.user.username").value("testuser"));
    }

    @Test
    void signIn_wrongPassword() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        when(authService.signIn(any(LoginRequest.class), anyString(), anyString()))
                .thenThrow(new BusinessException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void signIn_lockedAccount() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("lockeduser")
                .password("password123")
                .build();

        when(authService.signIn(any(LoginRequest.class), anyString(), anyString()))
                .thenThrow(new BusinessException("Account is locked due to too many failed attempts. Please try again later."));

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account is locked due to too many failed attempts. Please try again later."));
    }

    @Test
    void signOut_success() throws Exception {
        doNothing().when(authService).signOut(anyString());

        mockMvc.perform(post("/api/auth/sign-out")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getMe_authenticated() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "testuser", Set.of("REGULAR_USER"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .roles(List.of("REGULAR_USER"))
                .status("ACTIVE")
                .build();

        when(authService.getCurrentUser(1L)).thenReturn(userDto);

        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getMe_unauthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }
}
