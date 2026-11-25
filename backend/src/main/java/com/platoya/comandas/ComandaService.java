package com.platoya.comandas;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.platoya.catalogo.Producto;
import com.platoya.catalogo.ProductoRepository;
import com.platoya.common.error.NegocioException;
import com.platoya.common.error.RecursoNoEncontradoException;
import com.platoya.cuentas.Cuenta;
import com.platoya.cuentas.CuentaRepository;
import com.platoya.cuentas.EstadoCuenta;
import com.platoya.usuarios.UsuarioRepository;

@Service
public class ComandaService {

    static final String MENSAJE_CUENTA_INEXISTENTE = "La cuenta no existe.";
    static final String MENSAJE_CUENTA_NO_ABIERTA = "La cuenta no está abierta.";
    static final String MENSAJE_PRODUCTO_INEXISTENTE = "El producto no existe.";

    private final ComandaRepository comandas;
    private final ItemComandaRepository items;
    private final CuentaRepository cuentas;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;

    public ComandaService(ComandaRepository comandas, ItemComandaRepository items, CuentaRepository cuentas,
            ProductoRepository productos, UsuarioRepository usuarios) {
        this.comandas = comandas;
        this.items = items;
        this.cuentas = cuentas;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    // RN-09: todo o nada. Sin bloqueo: dos envíos simultáneos a la misma cuenta chocan contra
    // uq_comandas_cuenta_numero (500 hasta M06).
    @Transactional
    public ComandaCreada enviar(Long cuentaId, CrearComandaRequest request, Long mozoId) {
        Cuenta cuenta = cuentas.findById(cuentaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(MENSAJE_CUENTA_INEXISTENTE));
        if (cuenta.getEstado() != EstadoCuenta.ABIERTA) {
            throw new NegocioException("RN-04", MENSAJE_CUENTA_NO_ABIERTA);
        }
        Map<Long, Producto> disponibles = productosDisponibles(request.items());

        int numero = comandas.maximoNumero(cuentaId) + 1;
        // TIMESTAMPTZ guarda microsegundos: truncando, la respuesta coincide con lo que después devuelve GET items.
        OffsetDateTime fechaEnvio = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        Comanda comanda = comandas.save(
                Comanda.nueva(cuenta, usuarios.getReferenceById(mozoId), numero, fechaEnvio));
        List<ItemComanda> nuevos = items.saveAll(request.items().stream()
                .map(linea -> ItemComanda.pendiente(comanda, disponibles.get(linea.productoId()),
                        linea.cantidad(), normalizar(linea.observacion())))
                .toList());

        return new ComandaCreada(comanda.getId(), comanda.getNumero(), comanda.getFechaEnvio(),
                nuevos.stream().map(ItemPreparacion::de).toList());
    }

    @Transactional(readOnly = true)
    public List<ItemPreparacion> listarItems(Long cuentaId) {
        if (!cuentas.existsById(cuentaId)) {
            throw new RecursoNoEncontradoException(MENSAJE_CUENTA_INEXISTENTE);
        }
        return items.findByCuentaConMesa(cuentaId).stream().map(ItemPreparacion::de).toList();
    }

    // Una sola consulta para todas las líneas. Las repetidas (RN-11 ampliada) comparten el mismo producto.
    private Map<Long, Producto> productosDisponibles(List<CrearItemComandaRequest> lineas) {
        List<Long> ids = lineas.stream().map(CrearItemComandaRequest::productoId).distinct().toList();
        Map<Long, Producto> disponibles = productos.findDisponiblesByIdIn(ids).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));
        if (disponibles.size() != ids.size()) {
            throw new RecursoNoEncontradoException(MENSAJE_PRODUCTO_INEXISTENTE);
        }
        return disponibles;
    }

    private static String normalizar(String observacion) {
        return observacion == null || observacion.isBlank() ? null : observacion.trim();
    }
}
