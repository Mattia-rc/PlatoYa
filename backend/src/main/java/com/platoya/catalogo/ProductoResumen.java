package com.platoya.catalogo;

import java.math.BigDecimal;

public record ProductoResumen(Long id, String nombre, BigDecimal precio, Destino destino) {

    public static ProductoResumen de(Producto producto) {
        return new ProductoResumen(producto.getId(), producto.getNombre(), producto.getPrecio(),
                producto.getDestinoEfectivo());
    }
}
