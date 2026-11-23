package com.platoya.common.error;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

/**
 * Traduce las excepciones al formato de error común de la API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    static final String MENSAJE_VALIDACION = "Los datos enviados no son válidos.";
    static final String MENSAJE_FORMATO_INVALIDO = "El valor no tiene el formato esperado.";
    static final String MENSAJE_CUERPO_ILEGIBLE = "El cuerpo de la solicitud no es válido.";
    static final String MENSAJE_NO_ENCONTRADO = "El recurso solicitado no existe.";
    static final String MENSAJE_SOLICITUD_INVALIDA = "La solicitud no es válida.";
    static final String MENSAJE_INESPERADO = "Ocurrió un error inesperado. Intentá de nuevo más tarde.";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> detalles = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> detalles.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return validacionInvalida(detalles);
    }

    /** Parámetro de ruta o de consulta con un tipo inválido, por ejemplo {@code /mesas/abc}. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return validacionInvalida(Map.of(ex.getName(), MENSAJE_FORMATO_INVALIDO));
    }

    /** Validación de parámetros del controlador ({@code @RequestParam @Min(1)}, etc.) sin {@code @Validated}. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> parametrosInvalidos(HandlerMethodValidationException ex) {
        Map<String, String> detalles = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(resultado -> {
            String parametro = resultado.getMethodParameter().getParameterName();
            resultado.getResolvableErrors()
                    .forEach(error -> detalles.putIfAbsent(parametro, error.getDefaultMessage()));
        });
        return validacionInvalida(detalles);
    }

    /** Validación de métodos con {@code @Validated} (controladores o servicios). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> restriccionesVioladas(ConstraintViolationException ex) {
        Map<String, String> detalles = new LinkedHashMap<>();
        ex.getConstraintViolations()
                .forEach(violacion -> detalles.putIfAbsent(ultimoNodo(violacion.getPropertyPath()), violacion.getMessage()));
        return validacionInvalida(detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoIlegible(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.de(MENSAJE_CUERPO_ILEGIBLE));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> recursoNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.de(ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaInexistente(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.de(MENSAJE_NO_ENCONTRADO));
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErrorResponse> negocio(NegocioException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.conCodigo(ex.getMessage(), ex.getCodigo()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception ex) {
        // Las excepciones propias de Spring MVC (405, 415, etc.) traen su estado HTTP: se respeta
        // en lugar de convertirlas en 500.
        if (ex instanceof org.springframework.web.ErrorResponse errorMvc
                && errorMvc.getStatusCode().is4xxClientError()) {
            return ResponseEntity.status(errorMvc.getStatusCode()).body(ErrorResponse.de(MENSAJE_SOLICITUD_INVALIDA));
        }
        log.error("Error no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.de(MENSAJE_INESPERADO));
    }

    private static ResponseEntity<ErrorResponse> validacionInvalida(Map<String, String> detalles) {
        return ResponseEntity.badRequest().body(ErrorResponse.conDetalles(MENSAJE_VALIDACION, detalles));
    }

    /** De "crear.pedido.cantidad" se queda con "cantidad". */
    private static String ultimoNodo(Path ruta) {
        String nombre = null;
        for (Path.Node nodo : ruta) {
            nombre = nodo.getName();
        }
        return nombre;
    }
}
