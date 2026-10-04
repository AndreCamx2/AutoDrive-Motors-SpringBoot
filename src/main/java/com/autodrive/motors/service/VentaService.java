package com.autodrive.motors.service;

import com.autodrive.motors.dto.request.VentaRequestDTO;
import com.autodrive.motors.dto.response.VentaResponseDTO;

import java.util.List;

public interface VentaService {
    List<VentaResponseDTO> findAll();
    VentaResponseDTO registrarVenta(VentaRequestDTO dto);
}

