package com.platoya.cuentas;

import java.time.OffsetDateTime;

import com.platoya.mesas.Mesa;
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
@Table(name = "cuentas")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mozo_apertura_id", nullable = false)
    private Usuario mozoApertura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoCuenta estado;

    @Column(name = "fecha_apertura", nullable = false)
    private OffsetDateTime fechaApertura;

    @Column(name = "fecha_cuenta_pedida")
    private OffsetDateTime fechaCuentaPedida;

    @Column(name = "fecha_cierre")
    private OffsetDateTime fechaCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulada_por_id")
    private Usuario anuladaPor;

    @Column(name = "motivo_anulacion", length = 255)
    private String motivoAnulacion;

    protected Cuenta() {
    }

    public static Cuenta abrir(Mesa mesa, Usuario mozo, OffsetDateTime fechaApertura) {
        Cuenta cuenta = new Cuenta();
        cuenta.mesa = mesa;
        cuenta.mozoApertura = mozo;
        cuenta.estado = EstadoCuenta.ABIERTA;
        cuenta.fechaApertura = fechaApertura;
        return cuenta;
    }

    public Long getId() {
        return id;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public Usuario getMozoApertura() {
        return mozoApertura;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public OffsetDateTime getFechaApertura() {
        return fechaApertura;
    }

    public OffsetDateTime getFechaCuentaPedida() {
        return fechaCuentaPedida;
    }

    public OffsetDateTime getFechaCierre() {
        return fechaCierre;
    }

    public Usuario getAnuladaPor() {
        return anuladaPor;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }
}
