package com.autodrive.motors.service.impl;

import com.autodrive.motors.dto.request.ClienteRequestDTO;
import com.autodrive.motors.dto.response.ClienteResponseDTO;
import com.autodrive.motors.entity.Cliente;
import com.autodrive.motors.exception.DuplicateResourceException;
import com.autodrive.motors.exception.ResourceNotFoundException;
import com.autodrive.motors.repository.ClienteRepository;
import com.autodrive.motors.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> findAll() {
        return clienteRepository.findAll().stream()
                .map(ClienteResponseDTO::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO findById(Long id) {
        return ClienteResponseDTO.from(getClienteOrThrow(id));
    }

    @Override
    @Transactional
    public ClienteResponseDTO create(ClienteRequestDTO dto) {
        validarUnicidad(dto.cedula(), dto.email(), null);

        Cliente cliente = Cliente.builder()
                .cedula(dto.cedula())
                .nombre(dto.nombre())
                .apellido(dto.apellido())
                .email(dto.email())
                .telefono(dto.telefono())
                .direccion(dto.direccion())
                .build();

        return ClienteResponseDTO.from(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteResponseDTO update(Long id, ClienteRequestDTO dto) {
        Cliente cliente = getClienteOrThrow(id);
        validarUnicidad(dto.cedula(), dto.email(), id);

        cliente.setCedula(dto.cedula());
        cliente.setNombre(dto.nombre());
        cliente.setApellido(dto.apellido());
        cliente.setEmail(dto.email());
        cliente.setTelefono(dto.telefono());
        cliente.setDireccion(dto.direccion());

        return ClienteResponseDTO.from(cliente);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente", id);
        }
        clienteRepository.deleteById(id);
    }

    private Cliente getClienteOrThrow(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private void validarUnicidad(String cedula, String email, Long currentId) {
        boolean cedulaExiste = currentId == null 
                ? clienteRepository.existsByCedula(cedula)
                : clienteRepository.existsByCedulaAndIdNot(cedula, currentId);
        
        if (cedulaExiste) {
            throw new DuplicateResourceException("Cliente", "cédula", cedula);
        }

        boolean emailExiste = currentId == null 
                ? clienteRepository.existsByEmailIgnoreCase(email)
                : clienteRepository.existsByEmailIgnoreCaseAndIdNot(email, currentId);

        if (emailExiste) {
            throw new DuplicateResourceException("Cliente", "email", email);
        }
    }
}
