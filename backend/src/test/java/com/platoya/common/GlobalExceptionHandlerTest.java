package com.platoya.common;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.platoya.auth.JwtConfig;
import com.platoya.auth.SecurityConfig;
import com.platoya.common.error.NegocioException;
import com.platoya.common.error.RecursoNoEncontradoException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@WebMvcTest({ GlobalExceptionHandlerTest.ControladorPrueba.class, GlobalExceptionHandlerTest.ControladorValidado.class })
@Import({ GlobalExceptionHandlerTest.ControladorPrueba.class, GlobalExceptionHandlerTest.ControladorValidado.class,
        SecurityConfig.class, JwtConfig.class })
@WithMockUser
class GlobalExceptionHandlerTest {

    private static final String MENSAJE_VALIDACION = "Los datos enviados no son válidos.";
    private static final String MENSAJE_CANTIDAD = "La cantidad debe ser mayor a cero.";

    @Autowired
    private MockMvc mvc;

    @Test
    void validacion_devuelve400ConMensajeYDetalles() throws Exception {
        mvc.perform(post("/prueba/validacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_VALIDACION))
                .andExpect(jsonPath("$.detalles.nombre").value("El nombre es obligatorio."))
                .andExpect(jsonPath("$.codigo").doesNotExist());
    }

    @Test
    void parametroConTipoInvalido_devuelve400ConDetalles() throws Exception {
        mvc.perform(get("/prueba/mesas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_VALIDACION))
                .andExpect(jsonPath("$.detalles.id").value("El valor no tiene el formato esperado."));

        mvc.perform(get("/prueba/parametro").param("cantidad", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.cantidad").value("El valor no tiene el formato esperado."));
    }

    @Test
    void parametroInvalido_devuelve400ConDetalles() throws Exception {
        mvc.perform(get("/prueba/parametro").param("cantidad", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_VALIDACION))
                .andExpect(jsonPath("$.detalles.cantidad").value(MENSAJE_CANTIDAD));
    }

    @Test
    void parametroInvalidoConValidated_devuelve400ConDetalles() throws Exception {
        mvc.perform(get("/prueba/validado").param("cantidad", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_VALIDACION))
                .andExpect(jsonPath("$.detalles.cantidad").value(MENSAJE_CANTIDAD));
    }

    @Test
    void jsonMalFormado_devuelve400ConMensaje() throws Exception {
        mvc.perform(post("/prueba/validacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la solicitud no es válido."))
                .andExpect(jsonPath("$.detalles").doesNotExist());
    }

    @Test
    void metodoNoSoportado_conservaEstado405() throws Exception {
        mvc.perform(delete("/prueba/negocio"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.mensaje").value("La solicitud no es válida."));

        mvc.perform(post("/prueba/validacion")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("texto"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.mensaje").value("La solicitud no es válida."));
    }

    @Test
    void negocio_devuelve409ConMensajeYCodigo() throws Exception {
        mvc.perform(get("/prueba/negocio"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("La mesa ya tiene una cuenta activa."))
                .andExpect(jsonPath("$.codigo").value("RN-01"))
                .andExpect(jsonPath("$.detalles").doesNotExist());
    }

    @Test
    void recursoInexistente_devuelve404ConMensaje() throws Exception {
        mvc.perform(get("/prueba/recurso"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontró la mesa 99."));

        mvc.perform(get("/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("El recurso solicitado no existe."));
    }

    @Test
    void errorInesperado_devuelve500SinStacktrace() throws Exception {
        mvc.perform(get("/prueba/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Ocurrió un error inesperado. Intentá de nuevo más tarde."))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(content().string(not(containsString("detalle interno"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }

    record PedidoPrueba(@NotBlank(message = "El nombre es obligatorio.") String nombre) {
    }

    @RestController
    static class ControladorPrueba {

        @PostMapping("/prueba/validacion")
        void validacion(@Valid @RequestBody PedidoPrueba pedido) {
        }

        @GetMapping("/prueba/mesas/{id}")
        void mesa(@PathVariable Long id) {
        }

        @GetMapping("/prueba/parametro")
        void parametro(@RequestParam @Min(value = 1, message = MENSAJE_CANTIDAD) Integer cantidad) {
        }

        @GetMapping("/prueba/negocio")
        void negocio() {
            throw new NegocioException("RN-01", "La mesa ya tiene una cuenta activa.");
        }

        @GetMapping("/prueba/recurso")
        void recurso() {
            throw new RecursoNoEncontradoException("No se encontró la mesa 99.");
        }

        @GetMapping("/prueba/error")
        void error() {
            throw new IllegalStateException("detalle interno que no debe salir");
        }
    }

    /** Con {@code @Validated} la validación de parámetros lanza ConstraintViolationException. */
    @RestController
    @Validated
    static class ControladorValidado {

        @GetMapping("/prueba/validado")
        void validado(@RequestParam @Min(value = 1, message = MENSAJE_CANTIDAD) Integer cantidad) {
        }
    }
}
