package com.techforb.app.shared.auth;

import com.techforb.app.shared.security.CurrentUser;

/** Usuario de la sesión para el front (respuesta de login y de /me): misma forma que SessionUser del front. */
public record SessionUserResponse(String id, String name, String email, String role) {

    public static SessionUserResponse from(CurrentUser user) {
        return new SessionUserResponse(user.id(), user.name(), user.email(), user.role());
    }
}
