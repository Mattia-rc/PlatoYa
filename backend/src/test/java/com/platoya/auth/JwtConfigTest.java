package com.platoya.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class JwtConfigTest {

    private final JwtConfig config = new JwtConfig();

    @Test
    void secretoAusente_lanzaExcepcion() {
        assertThatThrownBy(() -> config.claveJwt(propiedades(null)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secretoDeMenosDe32Bytes_lanzaExcepcion() {
        assertThatThrownBy(() -> config.claveJwt(propiedades("a".repeat(31))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secretoDe32Bytes_creaLaClave() {
        assertThat(config.claveJwt(propiedades("a".repeat(32))).getAlgorithm()).isEqualTo("HmacSHA256");
    }

    private static JwtConfig.JwtProperties propiedades(String secreto) {
        return new JwtConfig.JwtProperties(secreto, Duration.ofHours(12));
    }
}
