package com.techforb.app.shared.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Firma y validación de los JWT de sesión con HS256 y el secreto de app.security.jwt-secret (JWT_SECRET, al menos 32
 * bytes). Nunca hay un secreto fijo en el código: sería público y cualquiera podría firmar sesiones. Sin JWT_SECRET,
 * en desarrollo se genera uno al azar en cada arranque (las sesiones se pierden al reiniciar); en producción
 * (COOKIE_SECURE=true, o sea HTTPS) la aplicación no arranca.
 */
@Configuration
public class JwtConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);

    private final SecretKey key;

    public JwtConfig(@Value("${app.security.jwt-secret:}") String secret, @Value("${app.security.cookie-secure}") boolean cookieSecure) {
        this.key = new SecretKeySpec(secretBytes(secret, cookieSecure), "HmacSHA256");
    }

    private static byte[] secretBytes(String secret, boolean cookieSecure) {
        if (secret != null && !secret.isBlank()) {
            byte[] bytes = secret.trim().getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET tiene que tener al menos 32 bytes");
            }
            return bytes;
        }
        if (cookieSecure) {
            throw new IllegalStateException("Falta JWT_SECRET: en producción es obligatorio (al menos 32 bytes al azar)");
        }
        log.warn("Sin JWT_SECRET: se usa un secreto al azar y las sesiones se pierden al reiniciar");
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        return random;
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
