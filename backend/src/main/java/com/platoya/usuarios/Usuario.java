package com.platoya.usuarios;

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
@Table(name = "usuarios")
public class Usuario {

    private static final String RN_BAJA_LOGICA = "RN-23";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "nombre_usuario", nullable = false, length = 50, unique = true)
    private String nombreUsuario;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Rol rol;

    @Column(name = "fecha_baja")
    private OffsetDateTime fechaBaja;

    protected Usuario() {
    }

    public Usuario(String nombre, String nombreUsuario, String passwordHash, Rol rol) {
        this.nombre = nombre;
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public void modificar(String nombre, Rol rol) {
        exigirActivo();
        this.nombre = nombre;
        this.rol = rol;
    }

    public void asignarPasswordHash(String passwordHash) {
        exigirActivo();
        this.passwordHash = passwordHash;
    }

    public void darDeBaja(OffsetDateTime fecha) {
        if (!estaActivo()) {
            throw new NegocioException(RN_BAJA_LOGICA, "El usuario ya está dado de baja.");
        }
        this.fechaBaja = fecha;
    }

    public void reactivar() {
        if (estaActivo()) {
            throw new NegocioException(RN_BAJA_LOGICA, "El usuario ya está activo.");
        }
        this.fechaBaja = null;
    }

    private void exigirActivo() {
        if (!estaActivo()) {
            throw new NegocioException(RN_BAJA_LOGICA, "El usuario está dado de baja. Reactivalo para modificarlo.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public OffsetDateTime getFechaBaja() {
        return fechaBaja;
    }

    public boolean estaActivo() {
        return fechaBaja == null;
    }
}
