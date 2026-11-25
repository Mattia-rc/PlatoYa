package com.platoya.comandas;

import java.time.OffsetDateTime;
import java.util.List;

public record ComandaCreada(Long id, Integer numero, OffsetDateTime fechaEnvio, List<ItemPreparacion> items) {
}
