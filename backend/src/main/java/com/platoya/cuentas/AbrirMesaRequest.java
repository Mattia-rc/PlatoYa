package com.platoya.cuentas;

import jakarta.validation.constraints.NotNull;

public record AbrirMesaRequest(@NotNull(message = "La mesa es obligatoria.") Long mesaId) {
}
