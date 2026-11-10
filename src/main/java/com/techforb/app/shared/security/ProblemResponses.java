package com.techforb.app.shared.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 401 y 403 de Spring Security con la misma forma que el resto de los errores (ProblemDetail con code), para que
 * el front los trate igual: 401 → vuelve al login, 403 → mensaje de permisos.
 */
final class ProblemResponses {

    private ProblemResponses() {
    }

    static AuthenticationEntryPoint unauthorized() {
        return (request, response, exception) -> write(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "UNAUTHORIZED",
                "Tu sesión no es válida o venció. Vuelve a iniciar sesión.");
    }

    static AccessDeniedHandler forbidden() {
        return (request, response, exception) -> write(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden", "FORBIDDEN",
                "No tienes permisos para realizar esta acción.");
    }

    private static void write(HttpServletResponse response, int status, String title, String code, String detail) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\",\"code\":\"%s\"}".formatted(title, status, detail, code));
    }
}
