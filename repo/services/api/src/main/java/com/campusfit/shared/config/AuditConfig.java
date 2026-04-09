package com.campusfit.shared.config;

import com.campusfit.shared.security.SecurityContextHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            String username = SecurityContextHelper.getCurrentUsername();
            return Optional.ofNullable(username);
        };
    }
}
