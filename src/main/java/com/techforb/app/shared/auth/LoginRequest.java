package com.techforb.app.shared.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Ingresa tu correo.") @Email(message = "Ingresa un correo válido.") String email,
        @NotBlank(message = "Ingresa tu contraseña.") String password) {
}
