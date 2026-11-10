package com.techforb.app.shared.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Usuario de la sesión, armado desde los claims del JWT (los que pone AuthService al iniciar sesión).
 * En un controller: {@code @AuthenticationPrincipal Jwt jwt} y {@code CurrentUser.from(jwt)}; en un service:
 * {@code CurrentUser.current()}. Con el resource server el principal es un Jwt: nunca uses
 * {@code @AuthenticationPrincipal CurrentUser}, llega null.
 */
public record CurrentUser(String id, String name, String email, String role) {

    public static final String CLAIM_NAME = "name";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLE = "role";

    public static CurrentUser from(Jwt jwt) {
        return new CurrentUser(jwt.getSubject(), jwt.getClaimAsString(CLAIM_NAME), jwt.getClaimAsString(CLAIM_EMAIL), jwt.getClaimAsString(CLAIM_ROLE));
    }

    /** Usuario de la request actual; vacío si no hay sesión (rutas públicas). */
    public static Optional<CurrentUser> current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof Jwt jwt ? Optional.of(from(jwt)) : Optional.empty();
    }

    public boolean hasRole(String... roles) {
        for (String candidate : roles) {
            if (candidate.equals(role)) {
                return true;
            }
        }
        return false;
    }
}
