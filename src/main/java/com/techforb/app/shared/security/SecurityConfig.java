package com.techforb.app.shared.security;

import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de la API: sesión JWT en cookie HttpOnly (resource server), todo /api/** autenticado salvo login y
 * logout, roles del claim "role" como ROLE_* (para @PreAuthorize("hasRole('ADMIN')")) y 401/403 como ProblemDetail.
 * CSRF deshabilitado: la cookie es SameSite=Lax y la API solo acepta JSON.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /** Rutas sin sesión. El resolver de la cookie tampoco lee el token en ellas. */
    public static final Set<String> PUBLIC_PATHS = Set.of("/api/auth/login", "/api/auth/logout");

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Value("${app.security.session-cookie}") String cookieName) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, PUBLIC_PATHS.toArray(String[]::new)).permitAll()
                        .requestMatchers("/actuator/health", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(resource -> resource
                        .bearerTokenResolver(new SessionCookieTokenResolver(cookieName, PUBLIC_PATHS))
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(ProblemResponses.unauthorized())
                        .accessDeniedHandler(ProblemResponses.forbidden()))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(ProblemResponses.unauthorized())
                        .accessDeniedHandler(ProblemResponses.forbidden()));
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(CurrentUser.CLAIM_ROLE);
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
