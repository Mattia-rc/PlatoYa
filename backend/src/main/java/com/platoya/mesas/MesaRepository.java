package com.platoya.mesas;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    List<Mesa> findByFechaBajaIsNullOrderByNumeroAsc();
}
