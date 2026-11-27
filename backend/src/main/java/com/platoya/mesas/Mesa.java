package com.platoya.mesas;

import java.time.OffsetDateTime;

import com.platoya.common.error.NegocioException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mesas")
public class Mesa {

    private static final String RN_BAJA_LOGICA = "RN-23";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer numero;

    @Column(nullable = false)
    private Integer capacidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoMesa estado = EstadoMesa.LIBRE;

    @Column(name = "fecha_baja")
    private OffsetDateTime fechaBaja;

    protected Mesa() {
    }

    public Mesa(Integer numero, Integer capacidad) {
        this.numero = numero;
        this.capacidad = capacidad;
    }

    public void modificar(Integer numero, Integer capacidad) {
        exigirActiva();
        this.numero = numero;
        this.capacidad = capacidad;
    }

    public void darDeBaja(OffsetDateTime fecha) {
        exigirActiva();
        if (estado == EstadoMesa.OCUPADA) {
            throw new NegocioException(null, "No se puede dar de baja una mesa ocupada.");
        }
        fechaBaja = fecha;
    }

    public void reactivar() {
        if (estaActiva()) {
            throw new NegocioException(RN_BAJA_LOGICA, "La mesa ya está activa.");
        }
        fechaBaja = null;
    }

    private void exigirActiva() {
        if (!estaActiva()) {
            throw new NegocioException(RN_BAJA_LOGICA, "La mesa está dada de baja. Reactivala para modificarla.");
        }
    }

    public Long getId() { return id; }
    public Integer getNumero() { return numero; }
    public Integer getCapacidad() { return capacidad; }
    public EstadoMesa getEstado() { return estado; }
    public OffsetDateTime getFechaBaja() { return fechaBaja; }
    public boolean estaActiva() { return fechaBaja == null; }
}
