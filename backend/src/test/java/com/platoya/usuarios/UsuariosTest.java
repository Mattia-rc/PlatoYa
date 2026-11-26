package com.platoya.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
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

/**
 * Escenarios de PYA-005 sobre los usuarios de V2. Los "Dado" crean a lucia y a pedro mediante la API.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UsuariosTest {

    private static final String RUTA = "/usuarios";
    private static final String MENSAJE_VALIDACION = "Los datos enviados no son válidos.";
    private static final String MENSAJE_REPETIDO =
            "Ya existe un usuario con ese nombre de usuario. Si está dado de baja, podés reactivarlo.";
    private static final String MENSAJE_INEXISTENTE = "El usuario no existe.";
    private static final String MENSAJE_CREDENCIALES = "Usuario o contraseña incorrectos.";
    private static final String MENSAJE_CONTRASENA =
            "La contraseña tiene que tener de 4 a 72 caracteres: letras sin tilde, dígitos o símbolos, sin espacios.";
    private static final long ID_INEXISTENTE = 999_999L;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void borrarUsuariosCreados() {
        jdbc.update("DELETE FROM usuarios WHERE nombre_usuario NOT IN ('admin', 'mozo', 'cocina', 'barra')");
    }

    @Test
    void altaUsuario_devuelve201SinHashEIniciaSesion() throws Exception {
        enviar(post(RUTA), token("admin"), cuerpoAlta("Lucía Mozo", "lucia", "abcd", "MOZO"))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith(RUTA + "/" + idDe("lucia"))))
                .andExpect(jsonPath("$.id").value(idDe("lucia")))
                .andExpect(jsonPath("$.nombre").value("Lucía Mozo"))
                .andExpect(jsonPath("$.usuario").value("lucia"))
                .andExpect(jsonPath("$.rol").value("MOZO"))
                .andExpect(jsonPath("$.fechaBaja").value(nullValue()))
                .andExpect(content().string(not(containsString("contrasena"))))
                .andExpect(content().string(not(containsString("passwordHash"))))
                .andExpect(content().string(not(containsString("password_hash"))));

        login("lucia", "abcd")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("MOZO"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Lucia", "lu", "lucía", "lucia perez" })
    void altaConUsuarioInvalido_devuelve400ConDetalle(String usuario) throws Exception {
        int antes = cantidadUsuarios();

        enviar(post(RUTA), token("admin"), cuerpoAlta("Lucía Mozo", usuario, "abcd", "MOZO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_VALIDACION))
                .andExpect(jsonPath("$.detalles.usuario").value(
                        "El usuario tiene que tener de 3 a 50 caracteres: letras minúsculas, dígitos, punto o guion bajo."));

        assertThat(cantidadUsuarios()).isEqualTo(antes);
    }

    @Test
    void altaConEspaciosAlrededor_guardaElUsuarioRecortado() throws Exception {
        enviar(post(RUTA), token("admin"), cuerpoAlta("  Lucía Mozo  ", "  lucia  ", "abcd", "MOZO"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario").value("lucia"))
                .andExpect(jsonPath("$.nombre").value("Lucía Mozo"));

        assertThat(idDe("lucia")).isNotNull();
    }

    @Test
    void altaConDatosInvalidos_devuelve400ConDetallePorCampo() throws Exception {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("nombre", "");
        cuerpo.put("usuario", "lucia");
        cuerpo.put("contrasena", "123");

        enviar(post(RUTA), token("admin"), cuerpo)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_VALIDACION))
                .andExpect(jsonPath("$.detalles.nombre").value("El nombre es obligatorio."))
                .andExpect(jsonPath("$.detalles.contrasena").value(MENSAJE_CONTRASENA))
                .andExpect(jsonPath("$.detalles.rol").value("El rol es obligatorio."));
    }

    @Test
    void altaConRolInexistente_devuelve400ConDetalleDelRol() throws Exception {
        enviar(post(RUTA), token("admin"), cuerpoAlta("Lucía Mozo", "lucia", "abcd", "JEFE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_VALIDACION))
                .andExpect(jsonPath("$.detalles.rol").value("El valor no tiene el formato esperado."));
    }

    @Test
    void altaConUsuarioRepetido_activoODadoDeBaja_devuelve409() throws Exception {
        String admin = token("admin");
        baja(alta("Pedro", "pedro", "abcd", "MOZO"));

        for (String usuario : List.of("mozo", "pedro")) {
            enviar(post(RUTA), admin, cuerpoAlta("Otro", usuario, "abcd", "MOZO"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.mensaje").value(MENSAJE_REPETIDO))
                    .andExpect(jsonPath("$.codigo").doesNotExist());
        }
    }

    @Test
    void listado_incluyeActivosYDadosDeBajaSinHash() throws Exception {
        baja(alta("Pedro", "pedro", "abcd", "MOZO"));

        ResultActions resultado = enviar(get(RUTA), token("admin"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].usuario").value(contains("admin", "barra", "cocina", "mozo", "pedro")))
                .andExpect(jsonPath("$[4].fechaBaja").value(notNullValue()))
                .andExpect(content().string(not(containsString("contrasena"))))
                .andExpect(content().string(not(containsString("passwordHash"))))
                .andExpect(content().string(not(containsString("password_hash"))));
        for (int i = 0; i < 4; i++) {
            resultado.andExpect(jsonPath("$[%d].fechaBaja".formatted(i)).value(nullValue()));
        }
    }

    @Test
    void modificacionSinContrasena_cambiaNombreYRolYConservaLaContrasena() throws Exception {
        Long id = alta("Lucía Mozo", "lucia", "abcd", "MOZO");

        enviar(put(RUTA + "/" + id), token("admin"), cuerpoModificacion("Lucía Barra", "BARRA", null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Lucía Barra"))
                .andExpect(jsonPath("$.usuario").value("lucia"))
                .andExpect(jsonPath("$.rol").value("BARRA"))
                .andExpect(jsonPath("$.fechaBaja").value(nullValue()));

        login("lucia", "abcd")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("BARRA"));
    }

    @Test
    void modificacionConContrasena_reemplazaLaAnterior() throws Exception {
        Long id = alta("Lucía Mozo", "lucia", "abcd", "MOZO");

        enviar(put(RUTA + "/" + id), token("admin"), cuerpoModificacion("Lucía Mozo", "MOZO", "nueva1"))
                .andExpect(status().isOk());

        login("lucia", "nueva1").andExpect(status().isOk());
        login("lucia", "abcd").andExpect(status().isUnauthorized());
    }

    @Test
    void modificacionConContrasenaVacia_devuelve400() throws Exception {
        Long id = alta("Lucía Mozo", "lucia", "abcd", "MOZO");

        enviar(put(RUTA + "/" + id), token("admin"), cuerpoModificacion("Lucía Mozo", "MOZO", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.contrasena").value(MENSAJE_CONTRASENA));

        login("lucia", "abcd").andExpect(status().isOk());
    }

    @Test
    void modificacionConOtroUsuario_loIgnora() throws Exception {
        Long id = alta("Lucía Mozo", "lucia", "abcd", "MOZO");
        Map<String, Object> cuerpo = cuerpoModificacion("Lucía Mozo", "MOZO", null);
        cuerpo.put("usuario", "lucia2");

        enviar(put(RUTA + "/" + id), token("admin"), cuerpo)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("lucia"));

        assertThat(jdbc.queryForObject("SELECT nombre_usuario FROM usuarios WHERE id = ?", String.class, id))
                .isEqualTo("lucia");
    }

    @Test
    void baja_completaFechaBajaYNoIniciaSesion() throws Exception {
        Long id = alta("Lucía Mozo", "lucia", "abcd", "MOZO");

        enviar(delete(RUTA + "/" + id), token("admin"), null).andExpect(status().isNoContent());

        assertThat(jdbc.queryForObject("SELECT fecha_baja IS NOT NULL FROM usuarios WHERE id = ?", Boolean.class, id))
                .isTrue();
        login("lucia", "abcd")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_CREDENCIALES));
    }

    @Test
    void reactivacion_quitaFechaBajaEIniciaSesion() throws Exception {
        Long id = alta("Lucía Mozo", "lucia", "abcd", "MOZO");
        baja(id);

        enviar(post(RUTA + "/" + id + "/reactivar"), token("admin"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("lucia"))
                .andExpect(jsonPath("$.fechaBaja").value(nullValue()));

        login("lucia", "abcd").andExpect(status().isOk());
    }

    @Test
    void adminNoSeDaDeBajaNiSeCambiaElRol_devuelve409() throws Exception {
        String admin = token("admin");
        Long idAdmin = idDe("admin");

        enviar(delete(RUTA + "/" + idAdmin), admin, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("No podés darte de baja a vos mismo."))
                .andExpect(jsonPath("$.codigo").doesNotExist());
        enviar(put(RUTA + "/" + idAdmin), admin, cuerpoModificacion("Administrador", "MOZO", null))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("No podés cambiar tu propio rol."))
                .andExpect(jsonPath("$.codigo").doesNotExist());

        Map<String, Object> fila = jdbc.queryForMap("SELECT rol, fecha_baja FROM usuarios WHERE id = ?", idAdmin);
        assertThat(fila.get("rol")).isEqualTo("ADMIN");
        assertThat(fila.get("fecha_baja")).isNull();
    }

    @Test
    void adminModificaSuPropioNombre_devuelve200() throws Exception {
        Long idAdmin = idDe("admin");
        String nombreOriginal = jdbc.queryForObject("SELECT nombre FROM usuarios WHERE id = ?", String.class, idAdmin);

        try {
            enviar(put(RUTA + "/" + idAdmin), token("admin"), cuerpoModificacion("Admin Nuevo", "ADMIN", null))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Admin Nuevo"))
                    .andExpect(jsonPath("$.rol").value("ADMIN"));
        } finally {
            jdbc.update("UPDATE usuarios SET nombre = ? WHERE id = ?", nombreOriginal, idAdmin);
        }
    }

    @Test
    void operacionesSobreEstadoQueNoCorresponde_devuelven409ConRN23() throws Exception {
        String admin = token("admin");
        Long idPedro = alta("Pedro", "pedro", "abcd", "MOZO");
        baja(idPedro);

        enviar(put(RUTA + "/" + idPedro), admin, cuerpoModificacion("Pedro Nuevo", "MOZO", null))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El usuario está dado de baja. Reactivalo para modificarlo."))
                .andExpect(jsonPath("$.codigo").value("RN-23"));
        enviar(delete(RUTA + "/" + idPedro), admin, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El usuario ya está dado de baja."))
                .andExpect(jsonPath("$.codigo").value("RN-23"));
        enviar(post(RUTA + "/" + idDe("mozo") + "/reactivar"), admin, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El usuario ya está activo."))
                .andExpect(jsonPath("$.codigo").value("RN-23"));
    }

    @Test
    void usuarioInexistente_devuelve404() throws Exception {
        String admin = token("admin");

        for (ResultActions resultado : List.of(
                enviar(put(RUTA + "/" + ID_INEXISTENTE), admin, cuerpoModificacion("Nadie", "MOZO", null)),
                enviar(delete(RUTA + "/" + ID_INEXISTENTE), admin, null),
                enviar(post(RUTA + "/" + ID_INEXISTENTE + "/reactivar"), admin, null))) {
            resultado.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.mensaje").value(MENSAJE_INEXISTENTE));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "mozo", "cocina", "barra" })
    void rolSinPermiso_devuelve403EnTodosLosEndpoints(String usuario) throws Exception {
        String token = token(usuario);
        int antes = cantidadUsuarios();

        for (ResultActions resultado : invocarTodosLosEndpoints(token)) {
            resultado.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.mensaje").value("No tenés permiso para realizar esta operación."));
        }
        assertThat(cantidadUsuarios()).isEqualTo(antes);
    }

    @Test
    void sinToken_devuelve401EnTodosLosEndpoints() throws Exception {
        for (ResultActions resultado : invocarTodosLosEndpoints(null)) {
            resultado.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.mensaje").value("Tenés que iniciar sesión."));
        }
    }

    private List<ResultActions> invocarTodosLosEndpoints(String token) throws Exception {
        Long idMozo = idDe("mozo");
        return List.of(
                enviar(get(RUTA), token, null),
                enviar(post(RUTA), token, cuerpoAlta("Lucía Mozo", "lucia", "abcd", "MOZO")),
                enviar(put(RUTA + "/" + idMozo), token, cuerpoModificacion("Mozo", "MOZO", null)),
                enviar(delete(RUTA + "/" + idMozo), token, null),
                enviar(post(RUTA + "/" + idMozo + "/reactivar"), token, null));
    }

    private Long alta(String nombre, String usuario, String contrasena, String rol) throws Exception {
        String respuesta = enviar(post(RUTA), token("admin"), cuerpoAlta(nombre, usuario, contrasena, rol))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(respuesta, "$.id")).longValue();
    }

    private void baja(Long id) throws Exception {
        enviar(delete(RUTA + "/" + id), token("admin"), null).andExpect(status().isNoContent());
    }

    private String token(String usuario) throws Exception {
        String respuesta = login(usuario, "1234")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    private ResultActions login(String usuario, String contrasena) throws Exception {
        return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("usuario", usuario, "contrasena", contrasena))));
    }

    private ResultActions enviar(MockHttpServletRequestBuilder pedido, String token, Map<String, Object> cuerpo)
            throws Exception {
        if (token != null) {
            pedido.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        if (cuerpo != null) {
            pedido.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cuerpo));
        }
        return mvc.perform(pedido);
    }

    private Long idDe(String usuario) {
        return jdbc.queryForObject("SELECT id FROM usuarios WHERE nombre_usuario = ?", Long.class, usuario);
    }

    private int cantidadUsuarios() {
        return jdbc.queryForObject("SELECT count(*) FROM usuarios", Integer.class);
    }

    private static Map<String, Object> cuerpoAlta(String nombre, String usuario, String contrasena, String rol) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("nombre", nombre);
        cuerpo.put("usuario", usuario);
        cuerpo.put("contrasena", contrasena);
        cuerpo.put("rol", rol);
        return cuerpo;
    }

    private static Map<String, Object> cuerpoModificacion(String nombre, String rol, String contrasena) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("nombre", nombre);
        cuerpo.put("rol", rol);
        if (contrasena != null) {
            cuerpo.put("contrasena", contrasena);
        }
        return cuerpo;
    }
}
