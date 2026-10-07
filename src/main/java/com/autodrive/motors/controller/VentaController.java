package com.autodrive.motors.controller;

import com.autodrive.motors.dto.request.VentaRequestDTO;
import com.autodrive.motors.dto.response.VentaResponseDTO;
import com.autodrive.motors.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.autodrive.motors.service.CurrencyExchangeService;
import com.autodrive.motors.dto.response.ConversionUsdDTO;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final CurrencyExchangeService currencyExchangeService;

    @GetMapping("/tasa-cambio")
    public ResponseEntity<ConversionUsdDTO> getTasaCambio() {
        return ResponseEntity.ok(currencyExchangeService.convertirCopAUsd(BigDecimal.ONE));
    }

    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> findAll() {
        return ResponseEntity.ok(ventaService.findAll());
    }

    @PostMapping
    public ResponseEntity<VentaResponseDTO> create(@RequestBody @Valid VentaRequestDTO dto) {
        VentaResponseDTO response = ventaService.registrarVenta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

