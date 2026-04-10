package com.campusfit.notification.controller;

import com.campusfit.notification.dto.DeliveryStatusResponse;
import com.campusfit.notification.dto.NotificationRequest;
import com.campusfit.notification.dto.NotificationResponse;
import com.campusfit.notification.entity.Notification;
import com.campusfit.notification.service.NotificationService;
import com.campusfit.shared.config.SecurityConfig;
import com.campusfit.shared.exception.GlobalExceptionHandler;
import com.campusfit.shared.security.AccessDeniedHandlerImpl;
import com.campusfit.shared.security.AuthenticationEntryPointImpl;
import com.campusfit.shared.security.SessionAuthenticationFilter;
import com.campusfit.shared.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private SessionAuthenticationFilter sessionAuthenticationFilter;

    @BeforeEach
    void setUpFilter() throws Exception {
        doAnswer(inv -> {
            FilterChain chain = inv.getArgument(2);
            chain.doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(sessionAuthenticationFilter).doFilter(any(), any(), any());
    }

    private void authenticateAs(Long userId, String username, Set<String> roles) {
        UserPrincipal principal = new UserPrincipal(userId, username, roles);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private NotificationResponse sampleNotification(Long id) {
        return NotificationResponse.builder()
                .id(id)
                .notificationId(100L)
                .type(Notification.NotificationType.ANNOUNCEMENT)
                .title("Test Notification")
                .body("Body text")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── Authentication ───────────────────────────────────────────────────────

    @Test
    void getNotifications_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void markAsRead_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/notifications/1/read"))
                .andExpect(status().isUnauthorized());
    }

    // ── Admin-only endpoints ─────────────────────────────────────────────────

    @Test
    void createNotification_nonAdmin_returns403() throws Exception {
        authenticateAs(2L, "user", Set.of("REGULAR_USER"));

        NotificationRequest request = NotificationRequest.builder()
                .type(Notification.NotificationType.ANNOUNCEMENT)
                .title("Hello")
                .targetUserIds(List.of(1L, 2L))
                .build();

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        SecurityContextHolder.clearContext();
    }

    @Test
    void getDeliveryStatus_nonAdmin_returns403() throws Exception {
        authenticateAs(2L, "user", Set.of("REGULAR_USER"));

        mockMvc.perform(get("/api/notifications/1/status"))
                .andExpect(status().isForbidden());

        SecurityContextHolder.clearContext();
    }

    // ── Validation ───────────────────────────────────────────────────────────

    @Test
    void createNotification_missingTitle_returns422() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        NotificationRequest request = NotificationRequest.builder()
                .type(Notification.NotificationType.ANNOUNCEMENT)
                // title intentionally omitted
                .targetUserIds(List.of(1L))
                .build();

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.title").value("Title is required"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void createNotification_emptyTargetList_returns422() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        NotificationRequest request = NotificationRequest.builder()
                .type(Notification.NotificationType.REMINDER)
                .title("Reminder")
                .targetUserIds(List.of())   // empty — @NotEmpty should reject
                .build();

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.targetUserIds").value("At least one target user is required"));

        SecurityContextHolder.clearContext();
    }

    // ── Happy paths ──────────────────────────────────────────────────────────

    @Test
    void getNotifications_authenticated_returns200WithPage() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(notificationService.getForUser(eq(1L), eq(0), eq(25)))
                .thenReturn(new PageImpl<>(
                        List.of(sampleNotification(1L), sampleNotification(2L)),
                        PageRequest.of(0, 25), 2));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].title").value("Test Notification"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void createNotification_admin_returns201() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        NotificationRequest request = NotificationRequest.builder()
                .type(Notification.NotificationType.ANNOUNCEMENT)
                .title("Campus Update")
                .body("Important update for all students.")
                .targetUserIds(List.of(10L, 11L, 12L))
                .build();

        doNothing().when(notificationService).create(any(NotificationRequest.class), eq(1L));

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        SecurityContextHolder.clearContext();
    }

    @Test
    void markAsRead_authenticated_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        doNothing().when(notificationService).markAsRead(eq(5L), eq(1L));

        mockMvc.perform(post("/api/notifications/5/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getDeliveryStatus_admin_returns200() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        when(notificationService.getDeliveryStatus(5L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/notifications/5/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        SecurityContextHolder.clearContext();
    }
}
