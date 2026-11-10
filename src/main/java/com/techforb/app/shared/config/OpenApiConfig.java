package com.techforb.app.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentación de la API en /swagger-ui.html; cada controller agrega su @Tag. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi(@Value("${spring.application.name}") String applicationName) {
        return new OpenAPI().info(new Info().title(applicationName).version("v1"));
    }
}
