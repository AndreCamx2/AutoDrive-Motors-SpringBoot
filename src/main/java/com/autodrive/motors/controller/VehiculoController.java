package com.autodrive.motors.controller;

import com.autodrive.motors.dto.request.VehiculoRequestDTO;
import com.autodrive.motors.dto.response.VehiculoResponseDTO;
import com.autodrive.motors.service.VehiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @GetMapping
    public ResponseEntity<List<VehiculoResponseDTO>> findAll() {
        return ResponseEntity.ok(vehiculoService.findAll());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<VehiculoResponseDTO>> findDisponibles() {
        return ResponseEntity.ok(vehiculoService.findDisponibles());
    }

    @GetMapping("/marca/{marca}")
    public ResponseEntity<List<VehiculoResponseDTO>> findByMarca(@PathVariable String marca) {
        return ResponseEntity.ok(vehiculoService.findByMarca(marca));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<VehiculoResponseDTO> create(@RequestBody @Valid VehiculoRequestDTO dto) {
        VehiculoResponseDTO response = vehiculoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculoResponseDTO> update(@PathVariable Long id, @RequestBody @Valid VehiculoRequestDTO dto) {
        return ResponseEntity.ok(vehiculoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehiculoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

