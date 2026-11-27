package com.platoya.mesas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class MesasTest {

    private static final String RUTA = "/mesas";
    private static final long ID_INEXISTENTE = 999_999L;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper objectMapper;

    @AfterEach
    void restaurarDatos() {
        jdbc.update("DELETE FROM mesas WHERE numero > 8");
        jdbc.update("UPDATE mesas SET estado = 'LIBRE', fecha_baja = NULL WHERE numero BETWEEN 1 AND 8");
    }

    @Test
    void alta_devuelve201YMesaLibre() throws Exception {
        enviar(post(RUTA), token("admin"), cuerpo(20, 6))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.numero").value(20))
                .andExpect(jsonPath("$.capacidad").value(6))
                .andExpect(jsonPath("$.estado").value("LIBRE"))
                .andExpect(jsonPath("$.fechaBaja").value(nullValue()));
    }

    @Test
    void datosInvalidos_devuelven400ConDetalle() throws Exception {
        enviar(post(RUTA), token("admin"), cuerpo(0, 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.numero").value("El número tiene que ser mayor que cero."))
                .andExpect(jsonPath("$.detalles.capacidad").value("La capacidad tiene que ser mayor que cero."));
    }

    @Test
    void numeroRepetidoActivoODadoDeBaja_devuelve409() throws Exception {
        String admin = token("admin");
        Long id = alta(20, 4);
        enviar(delete(RUTA + "/" + id), admin, null).andExpect(status().isNoContent());

        for (int numero : List.of(1, 20)) {
            enviar(post(RUTA), admin, cuerpo(numero, 4))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.mensaje").value(
                            "Ya existe una mesa con ese número. Si está dada de baja, podés reactivarla."))
                    .andExpect(jsonPath("$.codigo").doesNotExist());
        }
    }

    @Test
    void listadoFiltraBajasYOrdenaPorNumero() throws Exception {
        Long id = alta(20, 4);
        enviar(delete(RUTA + "/" + id), token("admin"), null).andExpect(status().isNoContent());

        enviar(get(RUTA), token("admin"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].numero").value(1));
        enviar(get(RUTA + "?incluirBajas=true"), token("admin"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(9)))
                .andExpect(jsonPath("$[8].numero").value(20))
                .andExpect(jsonPath("$[8].fechaBaja").value(notNullValue()));
    }

    @Test
    void modificacionActualizaNumeroYCapacidad() throws Exception {
        Long id = alta(20, 4);
        enviar(put(RUTA + "/" + id), token("admin"), cuerpo(21, 8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(21))
                .andExpect(jsonPath("$.capacidad").value(8));
    }

    @Test
    void bajaYReactivacionSonLogicas() throws Exception {
        String admin = token("admin");
        Long id = alta(20, 4);
        enviar(delete(RUTA + "/" + id), admin, null).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("SELECT fecha_baja IS NOT NULL FROM mesas WHERE id = ?", Boolean.class, id)).isTrue();

        enviar(post(RUTA + "/" + id + "/reactivar"), admin, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaBaja").value(nullValue()));
    }

    @Test
    void operacionesSobreEstadoIncorrecto_devuelven409ConRN23() throws Exception {
        String admin = token("admin");
        Long id = alta(20, 4);
        enviar(delete(RUTA + "/" + id), admin, null).andExpect(status().isNoContent());

        enviar(put(RUTA + "/" + id), admin, cuerpo(21, 5))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("RN-23"));
        enviar(delete(RUTA + "/" + id), admin, null)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("RN-23"));
        enviar(post(RUTA + "/1/reactivar"), admin, null)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("RN-23"));
    }

    @Test
    void mesaOcupadaNoSePuedeDarDeBaja() throws Exception {
        Long id = jdbc.queryForObject("SELECT id FROM mesas WHERE numero = 1", Long.class);
        jdbc.update("UPDATE mesas SET estado = 'OCUPADA' WHERE id = ?", id);

        enviar(delete(RUTA + "/" + id), token("admin"), null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("No se puede dar de baja una mesa ocupada."));
    }

    @Test
    void idInexistente_devuelve404() throws Exception {
        String admin = token("admin");
        for (ResultActions resultado : List.of(
                enviar(put(RUTA + "/" + ID_INEXISTENTE), admin, cuerpo(20, 4)),
                enviar(delete(RUTA + "/" + ID_INEXISTENTE), admin, null),
                enviar(post(RUTA + "/" + ID_INEXISTENTE + "/reactivar"), admin, null))) {
            resultado.andExpect(status().isNotFound()).andExpect(jsonPath("$.mensaje").value("La mesa no existe."));
        }
    }

    @Test
    void mozoPuedeListarPeroNoEscribir() throws Exception {
        String mozo = token("mozo");
        enviar(get(RUTA), mozo, null).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(8)));
        for (ResultActions resultado : invocarEscrituras(mozo)) {
            resultado.andExpect(status().isForbidden());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "cocina", "barra" })
    void rolSinPermiso_devuelve403(String usuario) throws Exception {
        enviar(get(RUTA), token(usuario), null).andExpect(status().isForbidden());
        for (ResultActions resultado : invocarEscrituras(token(usuario))) resultado.andExpect(status().isForbidden());
    }

    @Test
    void sinToken_devuelve401() throws Exception {
        enviar(get(RUTA), null, null).andExpect(status().isUnauthorized());
        for (ResultActions resultado : invocarEscrituras(null)) {
            resultado.andExpect(status().isUnauthorized());
        }
    }

    private List<ResultActions> invocarEscrituras(String token) throws Exception {
        return List.of(
                enviar(post(RUTA), token, cuerpo(20, 4)),
                enviar(put(RUTA + "/1"), token, cuerpo(1, 4)),
                enviar(delete(RUTA + "/1"), token, null),
                enviar(post(RUTA + "/1/reactivar"), token, null));
    }

    private Long alta(int numero, int capacidad) throws Exception {
        String respuesta = enviar(post(RUTA), token("admin"), cuerpo(numero, capacidad))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(respuesta, "$.id")).longValue();
    }

    private String token(String usuario) throws Exception {
        String respuesta = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("usuario", usuario, "contrasena", "1234"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    private ResultActions enviar(MockHttpServletRequestBuilder pedido, String token, Map<String, Object> cuerpo)
            throws Exception {
        if (token != null) pedido.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        if (cuerpo != null) pedido.contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cuerpo));
        return mvc.perform(pedido);
    }

    private static Map<String, Object> cuerpo(int numero, int capacidad) {
        return Map.of("numero", numero, "capacidad", capacidad);
    }
}
