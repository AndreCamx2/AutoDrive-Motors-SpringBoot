package com.autodrive.motors.dto.response;

import com.autodrive.motors.entity.Cliente;

import java.time.LocalDateTime;

public record ClienteResponseDTO(
        Long id,
        String cedula,
        String nombre,
        String apellido,
        String nombreCompleto,
        String email,
        String telefono,
        String direccion,
        LocalDateTime fechaRegistro
) {
    public static ClienteResponseDTO from(Cliente c) {
        return new ClienteResponseDTO(
                c.getId(),
                c.getCedula(),
                c.getNombre(),
                c.getApellido(),
                c.getNombreCompleto(),
                c.getEmail(),
                c.getTelefono(),
                c.getDireccion(),
                c.getFechaRegistro()
        );
    }
}
