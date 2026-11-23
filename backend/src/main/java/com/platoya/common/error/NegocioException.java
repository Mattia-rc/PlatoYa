package com.platoya.common.error;

/**
 * Violación de una regla de negocio. Se responde con 409 y el código de la regla (por ejemplo, "RN-01").
 */
public class NegocioException extends RuntimeException {

    private final String codigo;

    public NegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
