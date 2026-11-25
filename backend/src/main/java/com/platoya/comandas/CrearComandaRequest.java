package com.platoya.comandas;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CrearComandaRequest(
        @NotEmpty(message = "Los productos son obligatorios.")
        List<@NotNull(message = "La línea es obligatoria.") @Valid CrearItemComandaRequest> items) {
}
