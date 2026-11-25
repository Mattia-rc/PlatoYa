package com.platoya;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jayway.jsonpath.JsonPath;

/**
 * Prueba vertical de PYA-003 sobre los datos de V2, con login real y JWT.
 * Comparten la base: cada test usa su propia mesa y busca sus ítems por id, sin depender del orden.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RecorridoVerticalTest {

    private static final String CONTRASENA = "1234";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void recorridoCompleto_mozoAbreEnviaYCocinaLlevaHastaListo() throws Exception {
        String mozo = login("mozo");
        String cocina = login("cocina");

        String mesa = ejecutar(mozo, post("/cuentas").content("{\"mesaId\":" + idMesa(7) + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(7))
                .andExpect(jsonPath("$.estado").value("OCUPADA"))
                .andExpect(jsonPath("$.estadoCuenta").value("ABIERTA"))
                .andReturn().getResponse().getContentAsString();
        long cuentaId = leerId(mesa, "$.cuentaId");

        String comanda = enviarComanda(mozo, cuentaId, "Hamburguesa completa", "sin cebolla")
                .andExpect(jsonPath("$.numero").value(1))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.items[0].destino").value("COCINA"))
                .andReturn().getResponse().getContentAsString();
        long itemId = leerId(comanda, "$.items[0].id");
        // ItemPreparacion no expone el precio: se verifica la copia de RN-22 en la base.
        assertThat(jdbc.queryForObject("SELECT precio_unitario FROM items_comanda WHERE id = ?",
                BigDecimal.class, itemId)).isEqualByComparingTo("14000");

        ejecutar(cocina, get("/preparacion/items").param("destino", "COCINA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + itemId + ")].mesaNumero").value(7))
                .andExpect(jsonPath("$[?(@.id == " + itemId + ")].observacion").value("sin cebolla"))
                .andExpect(jsonPath("$[?(@.id == " + itemId + ")].fechaEnvio").isNotEmpty());

        cambiarEstado(cocina, itemId, "EN_PREPARACION")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PREPARACION"));
        cambiarEstado(cocina, itemId, "LISTO")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("LISTO"));

        ejecutar(mozo, get("/cuentas/{id}/items", cuentaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemId))
                .andExpect(jsonPath("$[0].estado").value("LISTO"));
    }

    @Test
    void abrirMesaOcupada_devuelve409ConRN01() throws Exception {
        String mozo = login("mozo");
        String body = "{\"mesaId\":" + idMesa(3) + "}";

        ejecutar(mozo, post("/cuentas").content(body)).andExpect(status().isCreated());

        ejecutar(mozo, post("/cuentas").content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("RN-01"))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    void login_contrasenaIncorrecta_devuelve401() throws Exception {
        intentarLogin("mozo", "incorrecta")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos."));
    }

    @Test
    void login_usuarioDadoDeBaja_devuelve401() throws Exception {
        jdbc.update("""
                INSERT INTO usuarios (nombre, nombre_usuario, password_hash, rol, fecha_baja)
                VALUES ('Mozo de baja', 'mozo_baja', ?, 'MOZO', now())
                """, passwordEncoder.encode(CONTRASENA));

        intentarLogin("mozo_baja", CONTRASENA)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos."));
    }

    @Test
    void sinToken_devuelve401() throws Exception {
        mvc.perform(get("/mesas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    void cocinaConsultaBarra_devuelve403() throws Exception {
        ejecutar(login("cocina"), get("/preparacion/items").param("destino", "BARRA"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    void mozoCambiaEstado_devuelve403() throws Exception {
        String mozo = login("mozo");
        long itemId = itemPendienteEnMesa(mozo, 4);

        cambiarEstado(mozo, itemId, "EN_PREPARACION")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
        assertThat(estadoEnBase(itemId)).isEqualTo("PENDIENTE");
    }

    @Test
    void pendienteAListoDirecto_devuelve409ConRN16YNoCambia() throws Exception {
        long itemId = itemPendienteEnMesa(login("mozo"), 5);

        cambiarEstado(login("cocina"), itemId, "LISTO")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("RN-16"))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
        assertThat(estadoEnBase(itemId)).isEqualTo("PENDIENTE");
        assertThat(jdbc.queryForObject("SELECT fecha_preparacion IS NULL AND fecha_listo IS NULL "
                + "FROM items_comanda WHERE id = ?", Boolean.class, itemId)).isTrue();
    }

    private long itemPendienteEnMesa(String mozo, int numeroMesa) throws Exception {
        String mesa = ejecutar(mozo, post("/cuentas").content("{\"mesaId\":" + idMesa(numeroMesa) + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String comanda = enviarComanda(mozo, leerId(mesa, "$.cuentaId"), "Hamburguesa completa", null)
                .andReturn().getResponse().getContentAsString();
        return leerId(comanda, "$.items[0].id");
    }

    private ResultActions enviarComanda(String token, long cuentaId, String producto, String observacion)
            throws Exception {
        String linea = "{\"productoId\":" + idProducto(token, producto) + ",\"cantidad\":1"
                + (observacion == null ? "" : ",\"observacion\":\"" + observacion + "\"") + "}";
        return ejecutar(token, post("/cuentas/{id}/comandas", cuentaId).content("{\"items\":[" + linea + "]}"))
                .andExpect(status().isCreated());
    }

    private ResultActions cambiarEstado(String token, long itemId, String estado) throws Exception {
        return ejecutar(token, patch("/preparacion/items/{id}/estado", itemId)
                .content("{\"estado\":\"" + estado + "\"}"));
    }

    private long idMesa(int numero) {
        return jdbc.queryForObject("SELECT id FROM mesas WHERE numero = ?", Long.class, numero);
    }

    private long idProducto(String token, String nombre) throws Exception {
        String productos = ejecutar(token, get("/productos"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(productos, "$[?(@.nombre == '" + nombre + "')].id");
        assertThat(ids).hasSize(1);
        return ids.get(0).longValue();
    }

    private String estadoEnBase(long itemId) {
        return jdbc.queryForObject("SELECT estado FROM items_comanda WHERE id = ?", String.class, itemId);
    }

    private String login(String usuario) throws Exception {
        String respuesta = intentarLogin(usuario, CONTRASENA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.usuario", equalTo(usuario)))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    private ResultActions intentarLogin(String usuario, String contrasena) throws Exception {
        return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":\"" + usuario + "\",\"contrasena\":\"" + contrasena + "\"}"));
    }

    private ResultActions ejecutar(String token, MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private static long leerId(String json, String ruta) {
        return ((Number) JsonPath.read(json, ruta)).longValue();
    }
}
