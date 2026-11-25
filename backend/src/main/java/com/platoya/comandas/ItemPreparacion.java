package com.platoya.comandas;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.platoya.catalogo.Destino;

// Requiere la comanda, la cuenta y la mesa del ítem ya cargadas (JOIN FETCH o dentro de la transacción).
public record ItemPreparacion(Long id, Long comandaId, Integer numeroComanda, Integer mesaNumero,
        String nombreProducto, Integer cantidad,
        @JsonInclude(JsonInclude.Include.NON_NULL) String observacion,
        EstadoItem estado, Destino destino, OffsetDateTime fechaEnvio) {

    public static ItemPreparacion de(ItemComanda item) {
        Comanda comanda = item.getComanda();
        return new ItemPreparacion(item.getId(), comanda.getId(), comanda.getNumero(),
                comanda.getCuenta().getMesa().getNumero(), item.getNombreProducto(), item.getCantidad(),
                item.getObservacion(), item.getEstado(), item.getDestino(), comanda.getFechaEnvio());
    }
}
