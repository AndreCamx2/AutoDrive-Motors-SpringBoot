package com.autodrive.motors.service.impl;

import com.autodrive.motors.dto.request.VehiculoRequestDTO;
import com.autodrive.motors.dto.response.VehiculoResponseDTO;
import com.autodrive.motors.entity.Vehiculo;
import com.autodrive.motors.entity.enums.EstadoVehiculo;
import com.autodrive.motors.exception.DuplicateResourceException;
import com.autodrive.motors.exception.ResourceNotFoundException;
import com.autodrive.motors.repository.VehiculoRepository;
import com.autodrive.motors.service.VehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> findAll() {
        return vehiculoRepository.findAll().stream()
                .map(VehiculoResponseDTO::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> findDisponibles() {
        return vehiculoRepository.findByEstado(EstadoVehiculo.DISPONIBLE).stream()
                .map(VehiculoResponseDTO::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> findByMarca(String marca) {
        return vehiculoRepository.findByMarcaIgnoreCase(marca).stream()
                .map(VehiculoResponseDTO::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponseDTO findById(Long id) {
        return VehiculoResponseDTO.from(getVehiculoOrThrow(id));
    }

    @Override
    @Transactional
    public VehiculoResponseDTO create(VehiculoRequestDTO dto) {
        validarUnicidadPlaca(dto.placa(), null);

        Vehiculo vehiculo = Vehiculo.builder()
                .placa(dto.placa())
                .marca(dto.marca())
                .modelo(dto.modelo())
                .anio(dto.anio())
                .color(dto.color())
                .precio(dto.precio())
                .build();

        return VehiculoResponseDTO.from(vehiculoRepository.save(vehiculo));
    }

    @Override
    @Transactional
    public VehiculoResponseDTO update(Long id, VehiculoRequestDTO dto) {
        Vehiculo vehiculo = getVehiculoOrThrow(id);
        validarUnicidadPlaca(dto.placa(), id);

        vehiculo.setPlaca(dto.placa());
        vehiculo.setMarca(dto.marca());
        vehiculo.setModelo(dto.modelo());
        vehiculo.setAnio(dto.anio());
        vehiculo.setColor(dto.color());
        vehiculo.setPrecio(dto.precio());

        return VehiculoResponseDTO.from(vehiculo);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehículo", id);
        }
        vehiculoRepository.deleteById(id);
    }

    private Vehiculo getVehiculoOrThrow(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", id));
    }

    private void validarUnicidadPlaca(String placa, Long currentId) {
        String placaNormalizada = placa != null ? placa.replaceAll("[\\s-]", "").toUpperCase() : null;
        
        boolean existe = currentId == null
                ? vehiculoRepository.existsByPlaca(placaNormalizada)
                : vehiculoRepository.existsByPlacaAndIdNot(placaNormalizada, currentId);

        if (existe) {
            throw new DuplicateResourceException("Vehículo", "placa", placaNormalizada);
        }
    }
}
