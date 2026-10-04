package com.autodrive.motors.dto.request;

import com.autodrive.motors.entity.enums.TipoMantenimiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record MantenimientoRequestDTO(
        @NotNull(message = "El ID del vehículo es obligatorio")
        Long vehiculoId,

        TipoMantenimiento tipo,

        @NotNull(message = "El costo es obligatorio")
        @PositiveOrZero(message = "El costo debe ser cero o mayor")
        BigDecimal costo,

        @NotBlank(message = "La descripción es obligatoria")
        String descripcion
) {
        public TipoMantenimiento tipoOrDefault() {
                return tipo != null ? tipo : TipoMantenimiento.REVISION;
        }
}
