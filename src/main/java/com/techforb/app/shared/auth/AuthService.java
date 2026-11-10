package com.techforb.app.shared.auth;

import com.techforb.app.shared.error.UnauthorizedException;
import com.techforb.app.shared.security.CurrentUser;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Valida credenciales contra los usuarios de prueba en memoria (DemoUsers) y emite el JWT de sesión con los claims
 * que lee CurrentUser. Mismo contrato que el backend con base de datos: el front no cambia al pasar a ese kit.
 */
@Service
public class AuthService {

    private final DemoUsers users;
    private final byte[] demoPassword;
    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final Duration sessionDuration;

    public AuthService(DemoUsers users, JwtEncoder jwtEncoder, Clock clock, @Value("${app.security.demo-password}") String demoPassword,
            @Value("${app.security.session-hours}") long sessionHours) {
        this.users = users;
        this.demoPassword = demoPassword.getBytes(StandardCharsets.UTF_8);
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
        this.sessionDuration = Duration.ofHours(sessionHours);
    }

    public Duration sessionDuration() {
        return sessionDuration;
    }

    /** Usuario con ese correo y la contraseña de prueba; si no, 401 con el mismo mensaje (no se revela si el correo existe). */
    public CurrentUser authenticate(LoginRequest request) {
        boolean passwordMatches = MessageDigest.isEqual(demoPassword, request.password().getBytes(StandardCharsets.UTF_8));
        return users.findByEmail(request.email().trim())
                .filter(user -> passwordMatches)
                .orElseThrow(() -> new UnauthorizedException("INVALID_CREDENTIALS", "El correo o la contraseña no son correctos."));
    }

    public String issueToken(CurrentUser user) {
        Instant now = Instant.now(clock);
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(user.id())
                .issuedAt(now)
                .expiresAt(now.plus(sessionDuration))
                .claim(CurrentUser.CLAIM_NAME, user.name())
                .claim(CurrentUser.CLAIM_EMAIL, user.email());
        // Sin rol no va el claim: JwtClaimsSet no acepta valores null.
        if (user.role() != null) {
            claims.claim(CurrentUser.CLAIM_ROLE, user.role());
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }
}
