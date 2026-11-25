package com.platoya.preparacion;

import com.platoya.comandas.EstadoItem;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoItemRequest(@NotNull(message = "El estado es obligatorio.") EstadoItem estado) {
}
