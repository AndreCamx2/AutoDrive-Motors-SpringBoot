package com.autodrive.motors.repository;

import com.autodrive.motors.entity.Vehiculo;
import com.autodrive.motors.entity.enums.EstadoVehiculo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByPlaca(String placa);

    boolean existsByPlacaAndIdNot(String placa, Long id);

    List<Vehiculo> findByEstado(EstadoVehiculo estado);

    List<Vehiculo> findByMarcaIgnoreCase(String marca);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vehiculo v WHERE v.id = :id")
    Optional<Vehiculo> findByIdForUpdate(@Param("id") Long id);
}
