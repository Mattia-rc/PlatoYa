package com.platoya.catalogo;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/productos")
@PreAuthorize("hasAnyRole('MOZO','ADMIN')")
public class ProductoController {

    private final ProductoRepository productos;

    public ProductoController(ProductoRepository productos) {
        this.productos = productos;
    }

    @GetMapping
    public List<ProductoResumen> listar() {
        return productos.findDisponibles().stream().map(ProductoResumen::de).toList();
    }
}
