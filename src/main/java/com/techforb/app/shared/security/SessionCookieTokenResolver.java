package com.techforb.app.shared.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

/**
 * Lee el JWT de la cookie de sesión (y, si no está, del header Authorization: Bearer). En las rutas públicas no
 * devuelve nada: una cookie vencida o firmada con otro secreto no puede dejar el login ni el logout en 401.
 */
public class SessionCookieTokenResolver implements BearerTokenResolver {

    private final String cookieName;
    private final Set<String> publicPaths;
    private final DefaultBearerTokenResolver headerResolver = new DefaultBearerTokenResolver();

    public SessionCookieTokenResolver(String cookieName, Set<String> publicPaths) {
        this.cookieName = cookieName;
        this.publicPaths = publicPaths;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        if (publicPaths.contains(request.getRequestURI())) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }
        return headerResolver.resolve(request);
    }
}
