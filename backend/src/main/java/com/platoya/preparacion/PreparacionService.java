package com.platoya.preparacion;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.platoya.catalogo.Destino;
import com.platoya.comandas.EstadoItem;
import com.platoya.comandas.ItemComanda;
import com.platoya.comandas.ItemComandaRepository;
import com.platoya.comandas.ItemPreparacion;
import com.platoya.common.error.NegocioException;
import com.platoya.common.error.RecursoNoEncontradoException;
import com.platoya.usuarios.Rol;

@Service
public class PreparacionService {

    static final String MENSAJE_ITEM_INEXISTENTE = "El ítem no existe.";
    static final String MENSAJE_TRANSICION_INVALIDA = "El ítem no puede pasar de %s a %s.";

    // ENTREGADO y ANULADO quedan fuera del spike; los anulados tachados (RN-21) entran con M07 y M09.
    private static final Set<EstadoItem> VISIBLES =
            EnumSet.of(EstadoItem.PENDIENTE, EstadoItem.EN_PREPARACION, EstadoItem.LISTO);

    private final ItemComandaRepository items;

    public PreparacionService(ItemComandaRepository items) {
        this.items = items;
    }

    @Transactional(readOnly = true)
    public List<ItemPreparacion> listar(Destino destino, Rol rol) {
        verificarSector(destino, rol);
        return items.findPorDestinoConMesa(destino, VISIBLES).stream().map(ItemPreparacion::de).toList();
    }

    // Sin bloqueo: dos cambios simultáneos sobre el mismo ítem pueden pisarse (pendiente para M07).
    @Transactional
    public ItemPreparacion cambiarEstado(Long itemId, EstadoItem nuevo, Rol rol) {
        ItemComanda item = items.findByIdConMesa(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException(MENSAJE_ITEM_INEXISTENTE));
        verificarSector(item.getDestino(), rol);
        EstadoItem actual = item.getEstado();
        // Truncada a microsegundos (lo que guarda TIMESTAMPTZ) para que la respuesta coincida con la base.
        if (!item.avanzarA(nuevo, OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS))) {
            throw new NegocioException("RN-16",
                    MENSAJE_TRANSICION_INVALIDA.formatted(nombreLegible(actual), nombreLegible(nuevo)));
        }
        return ItemPreparacion.de(item);
    }

    // RN-14: cada sector opera solo sus ítems. El handler la traduce a 403 con el mensaje genérico.
    private static void verificarSector(Destino destino, Rol rol) {
        Destino propio = switch (rol) {
            case COCINA -> Destino.COCINA;
            case BARRA -> Destino.BARRA;
            default -> null;
        };
        if (propio != destino) {
            throw new AccessDeniedException("RN-14: el rol " + rol + " no opera " + destino);
        }
    }

    private static String nombreLegible(EstadoItem estado) {
        return switch (estado) {
            case PENDIENTE -> "Pendiente";
            case EN_PREPARACION -> "En preparación";
            case LISTO -> "Listo";
            case ENTREGADO -> "Entregado";
            case ANULADO -> "Anulado";
        };
    }
}
