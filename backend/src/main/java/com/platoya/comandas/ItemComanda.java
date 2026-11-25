package com.platoya.comandas;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.platoya.catalogo.Destino;
import com.platoya.catalogo.Producto;
import com.platoya.usuarios.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "items_comanda")
public class ItemComanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comanda_id", nullable = false)
    private Comanda comanda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "nombre_producto", nullable = false, length = 120)
    private String nombreProducto;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Destino destino;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(length = 255)
    private String observacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoItem estado;

    @Column(name = "fecha_preparacion")
    private OffsetDateTime fechaPreparacion;

    @Column(name = "fecha_listo")
    private OffsetDateTime fechaListo;

    @Column(name = "fecha_entrega")
    private OffsetDateTime fechaEntrega;

    @Column(name = "fecha_anulacion")
    private OffsetDateTime fechaAnulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulado_por_id")
    private Usuario anuladoPor;

    @Column(name = "motivo_anulacion", length = 255)
    private String motivoAnulacion;

    @Column(nullable = false)
    private boolean merma;

    protected ItemComanda() {
    }

    // RN-22: nombre y precio se copian del producto para que un cambio posterior del catálogo no altere la comanda.
    // Sin fechas de etapa ni datos de anulación, cumple ck_items_fechas_estado, ck_items_anulacion y ck_items_merma.
    public static ItemComanda pendiente(Comanda comanda, Producto producto, int cantidad, String observacion) {
        ItemComanda item = new ItemComanda();
        item.comanda = comanda;
        item.producto = producto;
        item.nombreProducto = producto.getNombre();
        item.precioUnitario = producto.getPrecio();
        item.destino = producto.getDestinoEfectivo();
        item.cantidad = cantidad;
        item.observacion = observacion;
        item.estado = EstadoItem.PENDIENTE;
        item.merma = false;
        return item;
    }

    public Long getId() {
        return id;
    }

    public Comanda getComanda() {
        return comanda;
    }

    public Producto getProducto() {
        return producto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public Destino getDestino() {
        return destino;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public String getObservacion() {
        return observacion;
    }

    public EstadoItem getEstado() {
        return estado;
    }

    public OffsetDateTime getFechaPreparacion() {
        return fechaPreparacion;
    }

    public OffsetDateTime getFechaListo() {
        return fechaListo;
    }

    public OffsetDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public OffsetDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }

    public Usuario getAnuladoPor() {
        return anuladoPor;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public boolean isMerma() {
        return merma;
    }
}
