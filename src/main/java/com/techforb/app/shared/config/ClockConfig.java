package com.techforb.app.shared.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Hora actual inyectable: los services usan Instant.now(clock) / LocalDate.now(clock), nunca now() suelto. */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
