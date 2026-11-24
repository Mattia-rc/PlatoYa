package com.platoya.cuentas;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.platoya.mesas.MesaResumen;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/cuentas")
@PreAuthorize("hasAnyRole('MOZO','ADMIN')")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MesaResumen abrir(@Valid @RequestBody AbrirMesaRequest request, @AuthenticationPrincipal Jwt jwt) {
        return cuentaService.abrir(request.mesaId(), Long.valueOf(jwt.getSubject()));
    }
}
