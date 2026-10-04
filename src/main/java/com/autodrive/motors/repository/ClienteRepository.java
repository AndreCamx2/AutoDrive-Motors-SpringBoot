package com.autodrive.motors.repository;

import com.autodrive.motors.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCedula(String cedula);

    /** Para validar unicidad al actualizar, excluyendo el propio registro. */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByCedulaAndIdNot(String cedula, Long id);
}

