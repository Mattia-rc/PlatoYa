package com.platoya.preparacion;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.platoya.auth.AuthService;
import com.platoya.catalogo.Destino;
import com.platoya.comandas.ItemPreparacion;
import com.platoya.usuarios.Rol;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/preparacion/items")
@PreAuthorize("hasAnyRole('COCINA','BARRA')")
public class PreparacionController {

    private final PreparacionService preparacionService;

    public PreparacionController(PreparacionService preparacionService) {
        this.preparacionService = preparacionService;
    }

    @GetMapping
    public List<ItemPreparacion> listar(@RequestParam Destino destino, @AuthenticationPrincipal Jwt jwt) {
        return preparacionService.listar(destino, rol(jwt));
    }

    @PatchMapping("/{id}/estado")
    public ItemPreparacion cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoItemRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return preparacionService.cambiarEstado(id, request.estado(), rol(jwt));
    }

    private static Rol rol(Jwt jwt) {
        return Rol.valueOf(jwt.getClaimAsString(AuthService.CLAIM_ROL));
    }
}
