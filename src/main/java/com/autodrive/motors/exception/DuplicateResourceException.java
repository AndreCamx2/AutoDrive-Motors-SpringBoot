package com.autodrive.motors.exception;

/**
 * Intento de crear/actualizar un recurso con un valor único ya registrado (HTTP 409).
 * Ej: placa o email duplicados.
 */
public class DuplicateResourceException extends RuntimeException {

    private final String campo;

    public DuplicateResourceException(String recurso, String campo, Object valor) {
        super("Ya existe un %s con %s '%s'".formatted(recurso, campo, valor));
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
