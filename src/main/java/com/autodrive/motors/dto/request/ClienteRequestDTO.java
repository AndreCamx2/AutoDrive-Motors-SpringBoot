package com.autodrive.motors.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClienteRequestDTO(
        @NotBlank(message = "La cédula es obligatoria")
        @Pattern(regexp = "^\\d{5,15}$", message = "La cédula debe contener entre 5 y 15 dígitos numéricos")
        String cedula,

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        String apellido,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Debe ser un correo electrónico válido")
        String email,

        @Pattern(regexp = "^\\+?\\d{7,15}$", message = "El teléfono no tiene un formato válido")
        String telefono,

        String direccion
) {}
