package com.autodrive.motors.exception;

/**
 * Violación de una regla de negocio por el estado actual del recurso (HTTP 409).
 * Ej: vender un vehículo VENDIDO o EN_MANTENIMIENTO.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
