package com.platoya.comandas;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

// Vive en comandas y no en CuentaController para que el paquete cuentas no dependa de comandas.
@RestController
@RequestMapping("/cuentas/{cuentaId}")
public class ComandaController {

    private final ComandaService comandaService;

    public ComandaController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @PostMapping("/comandas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MOZO')")
    public ComandaCreada enviar(@PathVariable Long cuentaId, @Valid @RequestBody CrearComandaRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return comandaService.enviar(cuentaId, request, Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("/items")
    @PreAuthorize("hasAnyRole('MOZO','ADMIN')")
    public List<ItemPreparacion> listarItems(@PathVariable Long cuentaId) {
        return comandaService.listarItems(cuentaId);
    }
}
