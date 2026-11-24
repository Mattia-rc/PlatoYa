package com.platoya.cuentas;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platoya.mesas.Mesa;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    List<Cuenta> findByMesaInAndEstadoIn(Collection<Mesa> mesas, Collection<EstadoCuenta> estados);

    boolean existsByMesaAndEstadoIn(Mesa mesa, Collection<EstadoCuenta> estados);
}
