package com.autodrive.motors.service;

import com.autodrive.motors.dto.request.VehiculoRequestDTO;
import com.autodrive.motors.dto.response.VehiculoResponseDTO;

import java.util.List;

public interface VehiculoService {
    List<VehiculoResponseDTO> findAll();
    List<VehiculoResponseDTO> findDisponibles();
    List<VehiculoResponseDTO> findByMarca(String marca);
    VehiculoResponseDTO findById(Long id);
    VehiculoResponseDTO create(VehiculoRequestDTO dto);
    VehiculoResponseDTO update(Long id, VehiculoRequestDTO dto);
    void delete(Long id);
}

