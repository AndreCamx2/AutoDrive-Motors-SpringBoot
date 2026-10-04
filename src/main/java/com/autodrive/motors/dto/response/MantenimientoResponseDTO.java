package com.autodrive.motors.dto.response;

import com.autodrive.motors.entity.Mantenimiento;
import com.autodrive.motors.entity.enums.EstadoMantenimiento;
import com.autodrive.motors.entity.enums.TipoMantenimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MantenimientoResponseDTO(
        Long id,
        Long vehiculoId,
        String placa,
        TipoMantenimiento tipo,
        EstadoMantenimiento estado,
        String descripcion,
        BigDecimal costoCop,
        LocalDateTime fechaIngreso,
        LocalDateTime fechaSalida
) {
    public static MantenimientoResponseDTO from(Mantenimiento m) {
        return new MantenimientoResponseDTO(
                m.getId(),
                m.getVehiculo().getId(),
                m.getVehiculo().getPlaca(),
                m.getTipo(),
                m.getEstado(),
                m.getDescripcion(),
                m.getCosto(),
                m.getFechaIngreso(),
                m.getFechaSalida()
        );
    }
}
