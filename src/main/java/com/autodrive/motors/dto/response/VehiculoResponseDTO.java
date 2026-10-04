package com.autodrive.motors.dto.response;

import com.autodrive.motors.entity.Vehiculo;
import com.autodrive.motors.entity.enums.EstadoVehiculo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VehiculoResponseDTO(
        Long id,
        String placa,
        String marca,
        String modelo,
        Integer anio,
        String color,
        BigDecimal precioCop,
        EstadoVehiculo estado,
        LocalDateTime fechaCreacion
) {
    public static VehiculoResponseDTO from(Vehiculo v) {
        return new VehiculoResponseDTO(
                v.getId(),
                v.getPlaca(),
                v.getMarca(),
                v.getModelo(),
                v.getAnio(),
                v.getColor(),
                v.getPrecio(),
                v.getEstado(),
                v.getFechaCreacion()
        );
    }
}
