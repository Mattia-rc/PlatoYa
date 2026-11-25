package com.platoya.catalogo;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Disponible = producto y categoría sin baja. La categoría viene en la misma consulta para el destino efectivo.
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("""
            select p from Producto p join fetch p.categoria c
            where p.fechaBaja is null and c.fechaBaja is null
            order by c.nombre, p.nombre
            """)
    List<Producto> findDisponibles();

    @Query("""
            select p from Producto p join fetch p.categoria c
            where p.id in :ids and p.fechaBaja is null and c.fechaBaja is null
            """)
    List<Producto> findDisponiblesByIdIn(@Param("ids") Collection<Long> ids);
}
