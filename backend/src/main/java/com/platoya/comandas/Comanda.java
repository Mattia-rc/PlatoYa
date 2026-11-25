package com.platoya.comandas;

import java.time.OffsetDateTime;

import com.platoya.cuentas.Cuenta;
import com.platoya.usuarios.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "comandas")
public class Comanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mozo_id", nullable = false)
    private Usuario mozo;

    @Column(nullable = false)
    private Integer numero;

    @Column(name = "fecha_envio", nullable = false)
    private OffsetDateTime fechaEnvio;

    protected Comanda() {
    }

    public static Comanda nueva(Cuenta cuenta, Usuario mozo, int numero, OffsetDateTime fechaEnvio) {
        Comanda comanda = new Comanda();
        comanda.cuenta = cuenta;
        comanda.mozo = mozo;
        comanda.numero = numero;
        comanda.fechaEnvio = fechaEnvio;
        return comanda;
    }

    public Long getId() {
        return id;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public Usuario getMozo() {
        return mozo;
    }

    public Integer getNumero() {
        return numero;
    }

    public OffsetDateTime getFechaEnvio() {
        return fechaEnvio;
    }
}
