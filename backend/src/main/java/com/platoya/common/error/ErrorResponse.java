package com.platoya.common.error;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Cuerpo de error de la API. Coincide con {@code ApiError} del front:
 * {@code { mensaje, codigo?, detalles? }}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String mensaje, String codigo, Map<String, String> detalles) {

    public static ErrorResponse de(String mensaje) {
        return new ErrorResponse(mensaje, null, null);
    }

    public static ErrorResponse conCodigo(String mensaje, String codigo) {
        return new ErrorResponse(mensaje, codigo, null);
    }

    public static ErrorResponse conDetalles(String mensaje, Map<String, String> detalles) {
        return new ErrorResponse(mensaje, null, detalles);
    }
}
