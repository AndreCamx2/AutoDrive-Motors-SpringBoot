package com.autodrive.motors.repository;

import com.autodrive.motors.entity.Venta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    /** Carga cliente y vehículo en una sola consulta (evita N+1 al mapear a DTO). */
    @EntityGraph(attributePaths = {"cliente", "vehiculo"})
    List<Venta> findAllByOrderByFechaVentaDesc();

    @EntityGraph(attributePaths = {"cliente", "vehiculo"})
    List<Venta> findByClienteIdOrderByFechaVentaDesc(Long clienteId);

    @EntityGraph(attributePaths = {"cliente", "vehiculo"})
    Optional<Venta> findWithDetallesById(Long id);

    boolean existsByVehiculoId(Long vehiculoId);

    boolean existsByClienteId(Long clienteId);
}

