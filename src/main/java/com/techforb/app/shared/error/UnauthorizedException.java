package com.techforb.app.shared.error;

/** 401 con código (ej. INVALID_CREDENTIALS). Las rutas sin sesión las responde Spring Security, no esta excepción. */
public class UnauthorizedException extends RuntimeException {

    private final String code;

    public UnauthorizedException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
