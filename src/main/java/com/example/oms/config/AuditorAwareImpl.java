package com.example.oms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class JpaAuditingConfig {

    @Bean(name = "auditorAwareImpl")
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // In a real application, you would get this from Spring Security context
            // For example: SecurityContextHolder.getContext().getAuthentication().getName()
            // For now, using a system property or default value
            String auditor = System.getProperty("app.auditor", "SYSTEM");
            return Optional.of(auditor);
        };
    }
}

