package com.platoya.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio.") String usuario,
        @NotBlank(message = "La contraseña es obligatoria.") String contrasena) {
}
