package com.autodrive.motors.exception;

/**
 * Fallo de un servicio externo sin alternativa disponible (HTTP 503).
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
