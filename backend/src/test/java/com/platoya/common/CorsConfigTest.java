package com.platoya.common;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platoya.auth.JwtConfig;
import com.platoya.auth.SecurityConfig;

/** El preflight pasa por la cadena de Security real (sin token), como en producción. */
@WebMvcTest(CorsConfigTest.ControladorCors.class)
@Import({ CorsConfigTest.ControladorCors.class, SecurityConfig.class, JwtConfig.class })
class CorsConfigTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void preflight_desdeOrigenFront_esAceptado() throws Exception {
        mvc.perform(options("/prueba/cors")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        allOf(containsString("GET"), containsString("POST"), containsString("PUT"),
                                containsString("PATCH"), containsString("DELETE"))))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        allOf(containsStringIgnoringCase("authorization"),
                                containsStringIgnoringCase("content-type"))));
    }

    @Test
    void preflight_desdeOtroOrigen_esRechazado() throws Exception {
        mvc.perform(options("/prueba/cors")
                        .header(HttpHeaders.ORIGIN, "http://otro-origen.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @RestController
    static class ControladorCors {

        @PostMapping("/prueba/cors")
        void cors() {
        }
    }
}
