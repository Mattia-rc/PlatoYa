package com.platoya.usuarios;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioAltaRequest request) {
        UsuarioResponse creado = usuarioService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public UsuarioResponse modificar(@PathVariable Long id, @Valid @RequestBody UsuarioModificacionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.modificar(id, request, idActual(jwt));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        usuarioService.darDeBaja(id, idActual(jwt));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reactivar")
    public UsuarioResponse reactivar(@PathVariable Long id) {
        return usuarioService.reactivar(id);
    }

    private static Long idActual(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
