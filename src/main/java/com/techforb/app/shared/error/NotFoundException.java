package com.techforb.app.shared.error;

/** 404: el recurso pedido no existe. Mensaje para el usuario, en español. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
