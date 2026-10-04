package com.autodrive.motors.service;

import com.autodrive.motors.dto.request.MantenimientoRequestDTO;
import com.autodrive.motors.dto.response.MantenimientoResponseDTO;

import java.util.List;

public interface MantenimientoService {
    List<MantenimientoResponseDTO> findAll();
    MantenimientoResponseDTO registrarMantenimiento(MantenimientoRequestDTO dto);
}

