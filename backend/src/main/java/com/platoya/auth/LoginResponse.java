package com.platoya.auth;

public record LoginResponse(String token, UsuarioSesion usuario) {
}
