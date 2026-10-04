package com.autodrive.motors.entity.enums;

/**
 * Ciclo de vida comercial de un vehículo.
 */
public enum EstadoVehiculo {
    DISPONIBLE,
    VENDIDO,
    EN_MANTENIMIENTO;

    /** Regla de negocio #1: solo se venden vehículos DISPONIBLES. */
    public boolean permiteVenta() {
        return this == DISPONIBLE;
    }
}

