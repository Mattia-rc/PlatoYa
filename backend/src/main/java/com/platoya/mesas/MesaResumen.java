package com.platoya.mesas;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.platoya.cuentas.Cuenta;
import com.platoya.cuentas.EstadoCuenta;

// Sin cuenta activa, cuentaId y estadoCuenta no se serializan (son opcionales en api.models.ts).
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MesaResumen(Long id, Integer numero, Integer capacidad, EstadoMesa estado, Long cuentaId,
        EstadoCuenta estadoCuenta) {

    public static MesaResumen de(Mesa mesa, Cuenta cuentaActiva) {
        return new MesaResumen(mesa.getId(), mesa.getNumero(), mesa.getCapacidad(), mesa.getEstado(),
                cuentaActiva == null ? null : cuentaActiva.getId(),
                cuentaActiva == null ? null : cuentaActiva.getEstado());
    }
}
