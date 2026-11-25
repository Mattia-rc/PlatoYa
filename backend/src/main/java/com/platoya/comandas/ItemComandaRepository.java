package com.platoya.comandas;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemComandaRepository extends JpaRepository<ItemComanda, Long> {

    // Trae hasta la mesa en la misma consulta: ItemPreparacion necesita el número de comanda y de mesa.
    @Query("""
            select i from ItemComanda i
            join fetch i.comanda c
            join fetch c.cuenta cu
            join fetch cu.mesa
            where cu.id = :cuentaId
            order by c.numero, i.id
            """)
    List<ItemComanda> findByCuentaConMesa(@Param("cuentaId") Long cuentaId);
}
