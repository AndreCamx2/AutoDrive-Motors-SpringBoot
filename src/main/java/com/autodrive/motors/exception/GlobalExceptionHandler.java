package com.autodrive.motors.exception;

import com.autodrive.motors.dto.response.ApiErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Traduce todas las excepciones de la API a un {@link ApiErrorResponse} uniforme.
 *
 * <pre>
 * 400  Validación de campos / JSON mal formado / parámetros inválidos
 * 404  Recurso o ruta inexistente
 * 405  Método HTTP no soportado
 * 409  Duplicados (placa, email, cédula) y reglas de negocio por estado
 * 503  Servicio externo no disponible
 * 500  Error inesperado (sin exponer detalles internos)
 * </pre>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Nombres de constraints definidos en las entidades → mensaje amigable. */
    private static final Map<String, String> CONSTRAINT_MESSAGES = Map.of(
            "uk_vehiculos_placa", "Ya existe un vehículo con esa placa",
            "uk_clientes_email", "Ya existe un cliente con ese email",
            "uk_clientes_cedula", "Ya existe un cliente con esa cédula",
            "uk_ventas_vehiculo", "El vehículo ya fue vendido",
            "fk_ventas_cliente", "El cliente tiene ventas asociadas",
            "fk_ventas_vehiculo", "El vehículo tiene una venta asociada",
            "fk_mantenimientos_vehiculo", "El vehículo tiene mantenimientos asociados"
    );

    // ------------------------------------------------------------------ 400

    /** Errores de @Valid sobre @RequestBody: un mensaje limpio por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            // Si un campo tiene varias violaciones se concatenan
            errors.merge(fe.getField(), mensajeDe(fe), (a, b) -> a + "; " + b);
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(ge -> errors.put(ge.getObjectName(), ge.getDefaultMessage()));

        return build(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos", req, errors);
    }

    /** Validaciones sobre @PathVariable / @RequestParam (Spring 6.1+). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidation(HandlerMethodValidationException ex,
                                                                   HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(result -> {
            String param = result.getMethodParameter().getParameterName();
            String msg = result.getResolvableErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            errors.put(param != null ? param : "parametro", msg);
        });
        return build(HttpStatus.BAD_REQUEST, "Parámetros inválidos", req, errors);
    }

    /** Validaciones lanzadas desde servicios anotados con @Validated. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String path = v.getPropertyPath().toString();
            String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            errors.merge(field, v.getMessage(), (a, b) -> a + "; " + b);
        });
        return build(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos", req, errors);
    }

    /** JSON mal formado o valores con tipo incorrecto (ej. enum inexistente). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex,
                                                              HttpServletRequest req) {
        if (ex.getCause() instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
            String field = ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            Class<?> target = ife.getTargetType();
            String detalle = target.isEnum()
                    ? "Valor '%s' no permitido. Valores válidos: %s"
                        .formatted(ife.getValue(), Arrays.toString(target.getEnumConstants()))
                    : "Valor '%s' no es compatible con el tipo %s"
                        .formatted(ife.getValue(), target.getSimpleName());
            return build(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos", req,
                    Map.of(field, detalle));
        }
        return build(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud está vacío o no es un JSON válido", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest req) {
        Class<?> type = ex.getRequiredType();
        String detalle = (type != null && type.isEnum())
                ? "Valor '%s' no permitido. Valores válidos: %s"
                    .formatted(ex.getValue(), Arrays.toString(type.getEnumConstants()))
                : "Valor '%s' inválido".formatted(ex.getValue());
        return build(HttpStatus.BAD_REQUEST, "Parámetro con formato inválido", req,
                Map.of(ex.getName(), detalle));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException ex,
                                                               HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Falta un parámetro obligatorio", req,
                Map.of(ex.getParameterName(), "Es obligatorio"));
    }

    // ------------------------------------------------------------------ 404 / 405

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoRoute(NoResourceFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                     HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED,
                "Método %s no soportado en esta ruta".formatted(ex.getMethod()), req);
    }

    // ------------------------------------------------------------------ 409

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateResourceException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, Map.of(ex.getCampo(), ex.getMessage()));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(BusinessRuleException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    /**
     * Red de seguridad: si dos peticiones concurrentes pasan la validación previa,
     * la BD rechaza el duplicado y aquí se traduce a un 409 legible.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                HttpServletRequest req) {
        String raw = String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase(Locale.ROOT);
        String message = CONSTRAINT_MESSAGES.entrySet().stream()
                .filter(e -> raw.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("La operación viola una restricción de integridad de datos");
        log.warn("Violación de integridad en {}: {}", req.getRequestURI(), raw);
        return build(HttpStatus.CONFLICT, message, req);
    }

    // ------------------------------------------------------------------ 503 / 500

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleExternal(ExternalServiceException ex, HttpServletRequest req) {
        log.error("Fallo de servicio externo: {}", ex.getMessage(), ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {} {}", req.getMethod(), req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intente nuevamente o contacte al administrador", req);
    }

    // ------------------------------------------------------------------ helpers

    private static String mensajeDe(FieldError fe) {
        // Errores de conversión (ej. texto en un campo numérico) no traen mensaje amigable
        if (fe.isBindingFailure()) {
            return "Valor '%s' con formato inválido".formatted(fe.getRejectedValue());
        }
        return fe.getDefaultMessage();
    }

    private static ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        return build(status, message, req, null);
    }

    private static ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message,
                                                          HttpServletRequest req, Map<String, String> errors) {
        return ResponseEntity.status(status).body(
                ApiErrorResponse.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI(), errors));
    }
}
