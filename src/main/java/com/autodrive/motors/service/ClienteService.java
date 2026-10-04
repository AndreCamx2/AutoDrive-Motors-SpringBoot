package com.autodrive.motors.service;

import com.autodrive.motors.dto.request.ClienteRequestDTO;
import com.autodrive.motors.dto.response.ClienteResponseDTO;

import java.util.List;

public interface ClienteService {
    List<ClienteResponseDTO> findAll();
    ClienteResponseDTO findById(Long id);
    ClienteResponseDTO create(ClienteRequestDTO dto);
    ClienteResponseDTO update(Long id, ClienteRequestDTO dto);
    void delete(Long id);
}

