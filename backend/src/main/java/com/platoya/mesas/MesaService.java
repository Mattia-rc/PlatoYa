package com.platoya.mesas;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.platoya.common.error.NegocioException;
import com.platoya.common.error.RecursoNoEncontradoException;

@Service
@Transactional
public class MesaService {

    static final String RESTRICCION_NUMERO = "uq_mesas_numero";

    private final MesaRepository mesas;

    public MesaService(MesaRepository mesas) {
        this.mesas = mesas;
    }

    @Transactional(readOnly = true)
    public List<MesaResponse> listar(boolean incluirBajas) {
        List<Mesa> resultado = incluirBajas
                ? mesas.findAllByOrderByNumeroAsc()
                : mesas.findByFechaBajaIsNullOrderByNumeroAsc();
        return resultado.stream().map(MesaResponse::de).toList();
    }

    public MesaResponse crear(MesaRequest request) {
        return guardar(new Mesa(request.numero(), request.capacidad()));
    }

    public MesaResponse modificar(Long id, MesaRequest request) {
        Mesa mesa = buscarParaActualizar(id);
        mesa.modificar(request.numero(), request.capacidad());
        return guardar(mesa);
    }

    public void darDeBaja(Long id) {
        buscarParaActualizar(id).darDeBaja(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    public MesaResponse reactivar(Long id) {
        Mesa mesa = buscarParaActualizar(id);
        mesa.reactivar();
        return MesaResponse.de(mesa);
    }

    private MesaResponse guardar(Mesa mesa) {
        try {
            return MesaResponse.de(mesas.saveAndFlush(mesa));
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException violacion
                    && RESTRICCION_NUMERO.equals(violacion.getConstraintName())) {
                throw new NegocioException(null,
                        "Ya existe una mesa con ese número. Si está dada de baja, podés reactivarla.");
            }
            throw ex;
        }
    }

    private Mesa buscarParaActualizar(Long id) {
        return mesas.findByIdParaActualizar(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La mesa no existe."));
    }
}
