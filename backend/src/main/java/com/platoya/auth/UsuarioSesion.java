package com.platoya.auth;

import com.platoya.usuarios.Rol;
import com.platoya.usuarios.Usuario;

public record UsuarioSesion(Long id, String nombre, String usuario, Rol rol) {

    public static UsuarioSesion de(Usuario usuario) {
        return new UsuarioSesion(usuario.getId(), usuario.getNombre(), usuario.getNombreUsuario(), usuario.getRol());
    }
}
