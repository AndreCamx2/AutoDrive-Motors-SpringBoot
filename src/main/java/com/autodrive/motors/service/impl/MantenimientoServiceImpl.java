package com.autodrive.motors.service.impl;

import com.autodrive.motors.dto.request.MantenimientoRequestDTO;
import com.autodrive.motors.dto.response.MantenimientoResponseDTO;
import com.autodrive.motors.entity.Mantenimiento;
import com.autodrive.motors.entity.Vehiculo;
import com.autodrive.motors.entity.enums.EstadoVehiculo;
import com.autodrive.motors.exception.BusinessRuleException;
import com.autodrive.motors.exception.ResourceNotFoundException;
import com.autodrive.motors.repository.MantenimientoRepository;
import com.autodrive.motors.repository.VehiculoRepository;
import com.autodrive.motors.repository.VentaRepository;
import com.autodrive.motors.service.MantenimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MantenimientoServiceImpl implements MantenimientoService {

    private final MantenimientoRepository mantenimientoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final VentaRepository ventaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MantenimientoResponseDTO> findAll() {
        return mantenimientoRepository.findAllByOrderByFechaIngresoDesc().stream()
                .map(MantenimientoResponseDTO::from)
                .toList();
    }

    @Override
    @Transactional
    public MantenimientoResponseDTO registrarMantenimiento(MantenimientoRequestDTO dto) {
        Vehiculo vehiculo = vehiculoRepository.findByIdForUpdate(dto.vehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", dto.vehiculoId()));

        if (vehiculo.getEstado() == EstadoVehiculo.EN_MANTENIMIENTO) {
            throw new BusinessRuleException(
                    "No se puede registrar mantenimiento para un vehículo que ya está EN_MANTENIMIENTO."
            );
        }

        Mantenimiento mantenimiento = Mantenimiento.builder()
                .vehiculo(vehiculo)
                .tipo(dto.tipoOrDefault())
                .costo(dto.costo())
                .descripcion(dto.descripcion())
                .build();

        vehiculo.enviarAMantenimiento();

        return MantenimientoResponseDTO.from(mantenimientoRepository.save(mantenimiento));
    }

    @Override
    @Transactional
    public void finalizarMantenimiento(Long id) {
        Mantenimiento mantenimiento = mantenimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mantenimiento", id));

        mantenimiento.finalizar();

        Vehiculo vehiculo = mantenimiento.getVehiculo();
        if (ventaRepository.existsByVehiculoId(vehiculo.getId())) {
            vehiculo.marcarComoVendido();
        } else {
            vehiculo.marcarComoDisponible();
        }
        
        mantenimientoRepository.save(mantenimiento);
        vehiculoRepository.save(vehiculo);
    }

    @Override
    @Transactional
    public void eliminarMantenimiento(Long id) {
        Mantenimiento mantenimiento = mantenimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mantenimiento", id));

        // Si estaba en proceso, restauramos el estado del vehículo
        if (mantenimiento.getEstado() == com.autodrive.motors.entity.enums.EstadoMantenimiento.EN_PROCESO) {
            Vehiculo vehiculo = mantenimiento.getVehiculo();
            if (ventaRepository.existsByVehiculoId(vehiculo.getId())) {
                vehiculo.marcarComoVendido();
            } else {
                vehiculo.marcarComoDisponible();
            }
            vehiculoRepository.save(vehiculo);
        }

        mantenimientoRepository.delete(mantenimiento);
    }
}
