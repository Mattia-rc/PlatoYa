package com.platoya.mesas;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MesaRequest(
        @NotNull(message = "El número es obligatorio.")
        @Min(value = 1, message = "El número tiene que ser mayor que cero.")
        Integer numero,
        @NotNull(message = "La capacidad es obligatoria.")
        @Min(value = 1, message = "La capacidad tiene que ser mayor que cero.")
        Integer capacidad) {
}
