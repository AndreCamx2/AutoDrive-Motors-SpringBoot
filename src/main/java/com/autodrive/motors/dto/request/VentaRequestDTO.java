package com.autodrive.motors.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Solicitud de registro de venta. El precio, el descuento y la fecha
 * los calcula el servidor; nunca se aceptan desde el cliente.
 */
public record VentaRequestDTO(

        @NotNull(message = "El id del cliente es obligatorio")
        @Positive(message = "El id del cliente debe ser positivo")
        Long clienteId,

        @NotNull(message = "El id del vehículo es obligatorio")
        @Positive(message = "El id del vehículo debe ser positivo")
        Long vehiculoId,

        @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
        String observaciones
) {
}
