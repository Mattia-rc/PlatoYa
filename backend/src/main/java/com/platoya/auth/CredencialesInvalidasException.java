package com.platoya.auth;

// Mismo mensaje para usuario inexistente, contraseña incorrecta o dado de baja: no revela el motivo.
public class CredencialesInvalidasException extends RuntimeException {

    public static final String MENSAJE = "Usuario o contraseña incorrectos.";

    public CredencialesInvalidasException() {
        super(MENSAJE);
    }
}
