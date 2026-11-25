package com.platoya.comandas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComandaRepository extends JpaRepository<Comanda, Long> {

    @Query("select coalesce(max(c.numero), 0) from Comanda c where c.cuenta.id = :cuentaId")
    int maximoNumero(@Param("cuentaId") Long cuentaId);
}
