package com.techforb.app.shared.error;

/**
 * 422: una regla de negocio no se cumple. `code` es estable y en MAYÚSCULAS (ej. "POLICY_ALREADY_CANCELLED"):
 * el front puede decidir por él; el mensaje es para el usuario, en español.
 */
public class BusinessRuleException extends RuntimeException {

    private final String code;

    public BusinessRuleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
