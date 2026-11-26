package com.platoya.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Escenarios de PYA-004 sobre los usuarios de V2. Como todavía no hay endpoints de negocio,
 * el 401 y el 403 se prueban contra un controlador que existe solo en este test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AutenticacionTest {

    private static final String CONTRASENA = "1234";
    private static final String MENSAJE_CREDENCIALES = "Usuario o contraseña incorrectos.";
    private static final String MENSAJE_NO_AUTENTICADO = "Tenés que iniciar sesión.";
    private static final String MENSAJE_SIN_PERMISO = "No tenés permiso para realizar esta operación.";
    private static final String RUTA_AUTENTICADO = "/prueba/autenticado";
    private static final String RUTA_SOLO_ADMIN = "/prueba/solo-admin";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private SecretKey claveJwt;

    @ParameterizedTest
    @CsvSource({ "admin, ADMIN", "mozo, MOZO", "cocina, COCINA", "barra, BARRA" })
    void inicioSesionCorrecto_devuelveTokenYRolSinHash(String usuario, String rol) throws Exception {
        Long id = jdbc.queryForObject("SELECT id FROM usuarios WHERE nombre_usuario = ?", Long.class, usuario);
        String nombre = jdbc.queryForObject("SELECT nombre FROM usuarios WHERE id = ?", String.class, id);

        intentarLogin(usuario, CONTRASENA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.id").value(id))
                .andExpect(jsonPath("$.usuario.nombre").value(nombre))
                .andExpect(jsonPath("$.usuario.usuario").value(usuario))
                .andExpect(jsonPath("$.usuario.rol").value(rol))
                .andExpect(content().string(not(containsString("passwordHash"))))
                .andExpect(content().string(not(containsString("password_hash"))));
    }

    @Test
    void inicioSesionCorrecto_tokenVenceEn12Horas() throws Exception {
        var jwt = jwtDecoder.decode(login("mozo"));

        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(12));
    }

    @Test
    void credencialesInvalidas_contrasenaIncorrecta_devuelve401() throws Exception {
        intentarLogin("mozo", "incorrecta")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_CREDENCIALES));
    }

    @Test
    void credencialesInvalidas_usuarioInexistente_devuelve401ConElMismoMensaje() throws Exception {
        intentarLogin("no_existe", CONTRASENA)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_CREDENCIALES));
    }

    @Test
    void usuarioDadoDeBaja_devuelve401ConElMismoMensaje() throws Exception {
        jdbc.update("""
                INSERT INTO usuarios (nombre, nombre_usuario, password_hash, rol, fecha_baja)
                VALUES ('Mozo de baja', 'mozo_baja', ?, 'MOZO', now())
                """, passwordEncoder.encode(CONTRASENA));

        intentarLogin("mozo_baja", CONTRASENA)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_CREDENCIALES));
    }

    @Test
    void loginConDatosVacios_devuelve400ConDetallePorCampo() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Los datos enviados no son válidos."))
                .andExpect(jsonPath("$.detalles.usuario").value("El usuario es obligatorio."))
                .andExpect(jsonPath("$.detalles.contrasena").value("La contraseña es obligatoria."));
    }

    @Test
    void tokenValido_devuelve200() throws Exception {
        get(RUTA_AUTENTICADO, login("mozo")).andExpect(status().isOk());
    }

    @Test
    void tokenAusente_devuelve401() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(RUTA_AUTENTICADO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_NO_AUTENTICADO));
    }

    @Test
    void tokenVencido_devuelve401() throws Exception {
        // exp hace 1 h: supera el minuto de tolerancia del validador de Spring.
        Instant ahora = Instant.now();
        String vencido = token(ahora.minus(Duration.ofHours(13)), ahora.minus(Duration.ofHours(1)), claveJwt);

        get(RUTA_AUTENTICADO, vencido)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_NO_AUTENTICADO));
    }

    @Test
    void tokenFirmadoConOtraClave_devuelve401() throws Exception {
        SecretKey otraClave = new SecretKeySpec(
                "otra-clave-que-no-es-la-de-platoya-32".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Instant ahora = Instant.now();

        get(RUTA_AUTENTICADO, token(ahora, ahora.plus(Duration.ofHours(12)), otraClave))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_NO_AUTENTICADO));
    }

    @Test
    void tokenMalFormado_devuelve401() throws Exception {
        get(RUTA_AUTENTICADO, "no-es-un-jwt")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_NO_AUTENTICADO));
    }

    @Test
    void rolSinPermiso_cocinaEnOperacionDeAdmin_devuelve403() throws Exception {
        get(RUTA_SOLO_ADMIN, login("cocina"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_SIN_PERMISO));
    }

    @Test
    void rolConPermiso_adminEnOperacionDeAdmin_devuelve200() throws Exception {
        get(RUTA_SOLO_ADMIN, login("admin")).andExpect(status().isOk());
    }

    private String login(String usuario) throws Exception {
        String respuesta = intentarLogin(usuario, CONTRASENA)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    private ResultActions intentarLogin(String usuario, String contrasena) throws Exception {
        return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":\"" + usuario + "\",\"contrasena\":\"" + contrasena + "\"}"));
    }

    private ResultActions get(String ruta, String token) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.get(ruta).header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private static String token(Instant iat, Instant exp, SecretKey clave) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("1")
                .issuedAt(iat)
                .expiresAt(exp)
                .claim("usuario", "admin")
                .claim("rol", "ADMIN")
                .build();
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave))
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    @TestConfiguration
    static class ConfiguracionPrueba {

        @Bean
        ControladorPrueba controladorPrueba() {
            return new ControladorPrueba();
        }
    }

    @RestController
    static class ControladorPrueba {

        @GetMapping(RUTA_AUTENTICADO)
        String autenticado() {
            return "ok";
        }

        @GetMapping(RUTA_SOLO_ADMIN)
        @PreAuthorize("hasRole('ADMIN')")
        String soloAdmin() {
            return "ok";
        }
    }
}
