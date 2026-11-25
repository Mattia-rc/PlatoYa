package com.platoya.comandas;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearItemComandaRequest(
        @NotNull(message = "El producto es obligatorio.") Long productoId,
        @NotNull(message = "La cantidad es obligatoria.") @Min(value = 1, message = "La cantidad debe ser 1 o mayor.") Integer cantidad,
        @Size(max = 255, message = "La observación admite hasta 255 caracteres.") String observacion) {
}
