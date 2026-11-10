package com.techforb.app.shared.auth;

import com.techforb.app.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Sesión: login (cookie HttpOnly con el JWT), logout (borra la cookie) y el usuario actual. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Sesión")
public class AuthController {

    private final AuthService authService;
    private final String cookieName;
    private final boolean cookieSecure;

    public AuthController(AuthService authService, @Value("${app.security.session-cookie}") String cookieName,
            @Value("${app.security.cookie-secure}") boolean cookieSecure) {
        this.authService = authService;
        this.cookieName = cookieName;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/login")
    public SessionUserResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        CurrentUser user = authService.authenticate(request);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(authService.issueToken(user), authService.sessionDuration()).toString());
        return SessionUserResponse.from(user);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString());
    }

    @GetMapping("/me")
    public SessionUserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return SessionUserResponse.from(CurrentUser.from(jwt));
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(cookieName, value).httpOnly(true).secure(cookieSecure).sameSite("Lax").path("/").maxAge(maxAge).build();
    }
}
