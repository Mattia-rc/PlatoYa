package com.platoya.comandas;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.platoya.catalogo.Destino;

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

    @Query("""
            select i from ItemComanda i
            join fetch i.comanda c
            join fetch c.cuenta cu
            join fetch cu.mesa
            where i.destino = :destino and i.estado in :estados
            order by c.fechaEnvio, i.id
            """)
    List<ItemComanda> findPorDestinoConMesa(@Param("destino") Destino destino,
            @Param("estados") Collection<EstadoItem> estados);

    @Query("""
            select i from ItemComanda i
            join fetch i.comanda c
            join fetch c.cuenta cu
            join fetch cu.mesa
            where i.id = :id
            """)
    Optional<ItemComanda> findByIdConMesa(@Param("id") Long id);
}
