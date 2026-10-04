package com.autodrive.motors.exception;

/**
 * Recurso inexistente (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /** Ej: {@code new ResourceNotFoundException("Vehículo", 15L)} → "Vehículo con id 15 no encontrado". */
    public ResourceNotFoundException(String recurso, Object id) {
        super("%s con id %s no encontrado".formatted(recurso, id));
    }
}
