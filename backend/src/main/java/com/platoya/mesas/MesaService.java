package com.platoya.mesas;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.platoya.cuentas.Cuenta;
import com.platoya.cuentas.CuentaRepository;
import com.platoya.cuentas.EstadoCuenta;

@Service
public class MesaService {

    private final MesaRepository mesas;
    private final CuentaRepository cuentas;

    public MesaService(MesaRepository mesas, CuentaRepository cuentas) {
        this.mesas = mesas;
        this.cuentas = cuentas;
    }

    // Dos consultas (mesas y cuentas activas) en lugar de una por mesa.
    @Transactional(readOnly = true)
    public List<MesaResumen> listar() {
        List<Mesa> activas = mesas.findByFechaBajaIsNullOrderByNumeroAsc();
        Map<Long, Cuenta> cuentaPorMesa = cuentas.findByMesaInAndEstadoIn(activas, EstadoCuenta.ACTIVAS).stream()
                .collect(Collectors.toMap(cuenta -> cuenta.getMesa().getId(), Function.identity()));
        return activas.stream()
                .map(mesa -> MesaResumen.de(mesa, cuentaPorMesa.get(mesa.getId())))
                .toList();
    }
}
