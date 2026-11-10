package com.techforb.app.shared.error;

import java.util.List;
import java.util.Map;

/**
 * 409: la operación choca con el estado actual (ej. un código que ya existe). `field` es opcional: si viene,
 * el error también se informa en `errors` para que el front lo muestre en ese campo.
 */
public class ConflictException extends RuntimeException {

    private final String field;

    public ConflictException(String message) {
        this(message, null);
    }

    public ConflictException(String message, String field) {
        super(message);
        this.field = field;
    }

    public Map<String, List<String>> fieldErrors() {
        return field == null ? Map.of() : Map.of(field, List.of(getMessage()));
    }
}
