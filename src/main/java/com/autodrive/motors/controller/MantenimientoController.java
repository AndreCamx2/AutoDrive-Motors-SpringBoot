package com.autodrive.motors.controller;

import com.autodrive.motors.dto.request.MantenimientoRequestDTO;
import com.autodrive.motors.dto.response.MantenimientoResponseDTO;
import com.autodrive.motors.service.MantenimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mantenimientos")
@RequiredArgsConstructor
public class MantenimientoController {

    private final MantenimientoService mantenimientoService;

    @GetMapping
    public ResponseEntity<List<MantenimientoResponseDTO>> findAll() {
        return ResponseEntity.ok(mantenimientoService.findAll());
    }

    @PostMapping
    public ResponseEntity<MantenimientoResponseDTO> create(@RequestBody @Valid MantenimientoRequestDTO dto) {
        MantenimientoResponseDTO response = mantenimientoService.registrarMantenimiento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

