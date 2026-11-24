package com.platoya.cuentas;

import java.util.Set;

public enum EstadoCuenta {
    ABIERTA,
    CUENTA_PEDIDA,
    PAGADA,
    ANULADA;

    // Los mismos estados que el índice único parcial uq_cuentas_mesa_activa.
    public static final Set<EstadoCuenta> ACTIVAS = Set.of(ABIERTA, CUENTA_PEDIDA);
}
