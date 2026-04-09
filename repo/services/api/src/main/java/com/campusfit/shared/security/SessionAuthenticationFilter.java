package com.campusfit.shared.security;

import com.campusfit.auth.entity.Session;
import com.campusfit.auth.entity.UserRole;
import com.campusfit.auth.repository.RoleRepository;
import com.campusfit.auth.repository.SessionRepository;
import com.campusfit.auth.repository.UserRepository;
import com.campusfit.auth.repository.UserRoleRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final int sessionTimeoutMinutes;

    public SessionAuthenticationFilter(SessionRepository sessionRepository,
                                       UserRepository userRepository,
                                       UserRoleRepository userRoleRepository,
                                       RoleRepository roleRepository,
                                       @Value("${app.session.timeout-minutes:30}") int sessionTimeoutMinutes) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            Optional<Session> sessionOpt = sessionRepository.findByToken(token);

            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();

                // Check inactivity-based timeout using lastAccessedAt
                LocalDateTime lastActivity = session.getLastAccessedAt() != null
                        ? session.getLastAccessedAt()
                        : session.getCreatedAt();

                boolean isActive = lastActivity != null
                        && lastActivity.plusMinutes(sessionTimeoutMinutes).isAfter(LocalDateTime.now());

                if (isActive) {
                    userRepository.findById(session.getUserId()).ifPresent(user -> {
                        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
                        Set<String> roles = userRoles.stream()
                            .map(ur -> roleRepository.findById(ur.getRoleId())
                                .map(r -> r.getCode())
                                .orElse(null))
                            .filter(r -> r != null)
                            .collect(Collectors.toSet());

                        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), roles);

                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // Refresh last accessed time (sliding window)
                        session.setLastAccessedAt(LocalDateTime.now());
                        session.setExpiresAt(LocalDateTime.now().plusMinutes(sessionTimeoutMinutes));
                        sessionRepository.save(session);
                    });
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
