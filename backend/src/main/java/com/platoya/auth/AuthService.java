package com.platoya.auth;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.platoya.usuarios.Usuario;
import com.platoya.usuarios.UsuarioRepository;

@Service
public class AuthService {

    static final String CLAIM_USUARIO = "usuario";
    static final String CLAIM_ROL = "rol";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtConfig.JwtProperties propiedades;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder,
            JwtConfig.JwtProperties propiedades) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.propiedades = propiedades;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarios.findByNombreUsuario(request.usuario())
                .filter(Usuario::estaActivo)
                .filter(u -> passwordEncoder.matches(request.contrasena(), u.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);
        return new LoginResponse(emitirToken(usuario), UsuarioSesion.de(usuario));
    }

    private String emitirToken(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(ahora)
                .expiresAt(ahora.plus(propiedades.expiracion()))
                .claim(CLAIM_USUARIO, usuario.getNombreUsuario())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .build();
        // Con clave simétrica, Nimbus necesita el algoritmo explícito en el header.
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
