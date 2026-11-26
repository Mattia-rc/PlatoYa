package com.platoya.usuarios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioAltaRequest(
        @NotBlank(message = UsuarioAltaRequest.NOMBRE_OBLIGATORIO)
        @Size(max = 100, message = UsuarioAltaRequest.NOMBRE_LARGO)
        String nombre,
        @NotNull(message = "El usuario es obligatorio.")
        @Pattern(regexp = "^[a-z0-9._]{3,50}$",
                message = "El usuario tiene que tener de 3 a 50 caracteres: letras minúsculas, dígitos, punto o guion bajo.")
        String usuario,
        @NotNull(message = "La contraseña es obligatoria.")
        @Pattern(regexp = UsuarioAltaRequest.PATRON_CONTRASENA, message = UsuarioAltaRequest.CONTRASENA_INVALIDA)
        String contrasena,
        @NotNull(message = UsuarioAltaRequest.ROL_OBLIGATORIO)
        Rol rol) {

    static final String NOMBRE_OBLIGATORIO = "El nombre es obligatorio.";
    static final String NOMBRE_LARGO = "El nombre puede tener como máximo 100 caracteres.";
    // ASCII imprimible sin espacios: así 72 caracteres son 72 bytes, el máximo que admite BCrypt.
    static final String PATRON_CONTRASENA = "^[\\x21-\\x7E]{4,72}$";
    static final String CONTRASENA_INVALIDA =
            "La contraseña tiene que tener de 4 a 72 caracteres: letras sin tilde, dígitos o símbolos, sin espacios.";
    static final String ROL_OBLIGATORIO = "El rol es obligatorio.";

    public UsuarioAltaRequest {
        nombre = nombre == null ? null : nombre.strip();
        usuario = usuario == null ? null : usuario.strip();
    }
}
