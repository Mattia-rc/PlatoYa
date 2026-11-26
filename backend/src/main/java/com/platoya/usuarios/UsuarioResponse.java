package com.platoya.usuarios;

import java.time.OffsetDateTime;

public record UsuarioResponse(Long id, String nombre, String usuario, Rol rol, OffsetDateTime fechaBaja) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getNombreUsuario(), usuario.getRol(),
                usuario.getFechaBaja());
    }
}
