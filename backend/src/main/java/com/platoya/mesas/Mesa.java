package com.platoya.mesas;

import java.time.OffsetDateTime;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer numero;

    @Column(nullable = false)
    private Integer capacidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoMesa estado;

    @Column(name = "fecha_baja")
    private OffsetDateTime fechaBaja;

    protected Mesa() {
    }

    public Long getId() {
        return id;
    }

    public Integer getNumero() {
        return numero;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public EstadoMesa getEstado() {
        return estado;
    }

    public OffsetDateTime getFechaBaja() {
        return fechaBaja;
    }

    public boolean estaActiva() {
        return fechaBaja == null;
    }

    public void ocupar() {
        this.estado = EstadoMesa.OCUPADA;
    }
}
