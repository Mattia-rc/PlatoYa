package com.platoya;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Escenario "Flyway crea el esquema y carga los datos de prueba":
 * levanta el contexto completo contra un PostgreSQL 16 vacío.
 */
@SpringBootTest
@Testcontainers
class MigracionesTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void flyway_aplicaV1yV2_yJpaValidaEsquema() {
        // Si ddl-auto=validate fallara, el contexto no levantaría y el test no llegaría acá.
        List<MigrationInfo> aplicadas = Arrays.stream(flyway.info().applied()).toList();

        assertThat(aplicadas)
                .extracting(info -> info.getVersion().getVersion())
                .containsExactly("1", "2");
        assertThat(aplicadas)
                .extracting(MigrationInfo::getState)
                .containsOnly(MigrationState.SUCCESS);
    }

    @Test
    void datosPrueba_cargaUnUsuarioActivoPorRolConBCrypt() {
        List<Map<String, Object>> usuarios = jdbc.queryForList(
                "SELECT nombre_usuario, rol, password_hash FROM usuarios WHERE fecha_baja IS NULL ORDER BY id");

        assertThat(usuarios)
                .extracting(u -> u.get("rol"))
                .containsExactlyInAnyOrder("ADMIN", "MOZO", "COCINA", "BARRA");
        assertThat(usuarios)
                .extracting(u -> u.get("nombre_usuario"))
                .containsExactlyInAnyOrder("admin", "mozo", "cocina", "barra");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertThat(usuarios).allSatisfy(u -> {
            String hash = (String) u.get("password_hash");
            assertThat(hash).startsWith("$2a$");
            assertThat(encoder.matches("1234", hash)).isTrue();
        });
    }

    @Test
    void datosPrueba_cargaMesasLibresYProductosDelCaso5() {
        List<Map<String, Object>> mesas = jdbc.queryForList(
                "SELECT numero, capacidad, estado FROM mesas WHERE fecha_baja IS NULL ORDER BY numero");

        assertThat(mesas).extracting(m -> m.get("numero")).containsExactly(1, 2, 3, 4, 5, 6, 7, 8);
        assertThat(mesas).extracting(m -> m.get("estado")).containsOnly("LIBRE");
        assertThat(mesas.get(6).get("capacidad")).isEqualTo(4);

        // Destino efectivo: el del producto o, si no tiene, el de su categoría (RN-13).
        List<Map<String, Object>> productos = jdbc.queryForList("""
                SELECT p.nombre, p.precio, COALESCE(p.destino, c.destino) AS destino
                FROM productos p JOIN categorias c ON c.id = p.categoria_id
                WHERE p.fecha_baja IS NULL
                ORDER BY p.id
                """);

        assertThat(productos).hasSize(4);
        assertProducto(productos.get(0), "Cerveza (pinta)", "6500.00", "BARRA");
        assertProducto(productos.get(1), "Gaseosa", "3500.00", "BARRA");
        assertProducto(productos.get(2), "Hamburguesa completa", "14000.00", "COCINA");
        assertProducto(productos.get(3), "Porción de papas", "7000.00", "COCINA");
    }

    private static void assertProducto(Map<String, Object> fila, String nombre, String precio, String destino) {
        assertThat(fila.get("nombre")).isEqualTo(nombre);
        assertThat((BigDecimal) fila.get("precio")).isEqualByComparingTo(precio);
        assertThat(fila.get("destino")).isEqualTo(destino);
    }
}
