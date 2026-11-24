package com.platoya.cuentas;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.platoya.common.error.NegocioException;
import com.platoya.common.error.RecursoNoEncontradoException;
import com.platoya.mesas.EstadoMesa;
import com.platoya.mesas.Mesa;
import com.platoya.mesas.MesaRepository;
import com.platoya.mesas.MesaResumen;
import com.platoya.usuarios.UsuarioRepository;

@Service
public class CuentaService {

    static final String MENSAJE_MESA_INEXISTENTE = "La mesa no existe.";
    static final String MENSAJE_MESA_OCUPADA = "La mesa ya está ocupada.";

    private final CuentaRepository cuentas;
    private final MesaRepository mesas;
    private final UsuarioRepository usuarios;

    public CuentaService(CuentaRepository cuentas, MesaRepository mesas, UsuarioRepository usuarios) {
        this.cuentas = cuentas;
        this.mesas = mesas;
        this.usuarios = usuarios;
    }

    // Sin bloqueo: dos aperturas simultáneas chocan contra uq_cuentas_mesa_activa (500 hasta M05 y M06).
    @Transactional
    public MesaResumen abrir(Long mesaId, Long mozoId) {
        Mesa mesa = mesas.findById(mesaId)
                .filter(Mesa::estaActiva)
                .orElseThrow(() -> new RecursoNoEncontradoException(MENSAJE_MESA_INEXISTENTE));
        if (mesa.getEstado() == EstadoMesa.OCUPADA || cuentas.existsByMesaAndEstadoIn(mesa, EstadoCuenta.ACTIVAS)) {
            throw new NegocioException("RN-01", MENSAJE_MESA_OCUPADA);
        }
        // La fecha la pone el servicio y no el DEFAULT de la base, para tenerla sin recargar la entidad.
        Cuenta cuenta = cuentas.save(Cuenta.abrir(mesa, usuarios.getReferenceById(mozoId), OffsetDateTime.now()));
        mesa.ocupar();
        return MesaResumen.de(mesa, cuenta);
    }
}
