package com.platoya.mesas;

import java.time.OffsetDateTime;

public record MesaResponse(Long id, Integer numero, Integer capacidad, EstadoMesa estado, OffsetDateTime fechaBaja) {
    public static MesaResponse de(Mesa mesa) {
        return new MesaResponse(mesa.getId(), mesa.getNumero(), mesa.getCapacidad(), mesa.getEstado(),
                mesa.getFechaBaja());
    }
}
