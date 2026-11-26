package com.platoya.usuarios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Sin {@code usuario}: el nombre de usuario no se modifica. Una {@code contrasena} null conserva la anterior. */
public record UsuarioModificacionRequest(
        @NotBlank(message = UsuarioAltaRequest.NOMBRE_OBLIGATORIO)
        @Size(max = 100, message = UsuarioAltaRequest.NOMBRE_LARGO)
        String nombre,
        @NotNull(message = UsuarioAltaRequest.ROL_OBLIGATORIO)
        Rol rol,
        @Pattern(regexp = UsuarioAltaRequest.PATRON_CONTRASENA, message = UsuarioAltaRequest.CONTRASENA_INVALIDA)
        String contrasena) {

    public UsuarioModificacionRequest {
        nombre = nombre == null ? null : nombre.strip();
    }
}
