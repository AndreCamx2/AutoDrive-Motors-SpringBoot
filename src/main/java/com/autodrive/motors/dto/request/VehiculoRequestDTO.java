package com.autodrive.motors.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record VehiculoRequestDTO(
        @NotBlank(message = "La placa es obligatoria")
        String placa,

        @NotBlank(message = "La marca es obligatoria")
        String marca,

        @NotBlank(message = "El modelo es obligatorio")
        String modelo,

        @NotNull(message = "El año es obligatorio")
        @Positive(message = "El año debe ser válido")
        Integer anio,

        @NotBlank(message = "El color es obligatorio")
        String color,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser un valor positivo en COP")
        BigDecimal precio
) {}
