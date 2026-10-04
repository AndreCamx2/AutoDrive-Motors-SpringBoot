package com.autodrive.motors.service.impl;

import com.autodrive.motors.dto.request.VentaRequestDTO;
import com.autodrive.motors.dto.response.ConversionUsdDTO;
import com.autodrive.motors.dto.response.VentaResponseDTO;
import com.autodrive.motors.entity.Cliente;
import com.autodrive.motors.entity.Vehiculo;
import com.autodrive.motors.entity.Venta;
import com.autodrive.motors.exception.BusinessRuleException;
import com.autodrive.motors.exception.ResourceNotFoundException;
import com.autodrive.motors.repository.ClienteRepository;
import com.autodrive.motors.repository.VehiculoRepository;
import com.autodrive.motors.repository.VentaRepository;
import com.autodrive.motors.service.CurrencyExchangeService;
import com.autodrive.motors.service.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final CurrencyExchangeService currencyExchangeService;

    @Value("${business.venta.umbral-descuento:100000000}")
    private BigDecimal umbralDescuento;

    @Value("${business.venta.porcentaje-descuento:5}")
    private BigDecimal porcentajeDescuento;

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> findAll() {
        return ventaRepository.findAllByOrderByFechaVentaDesc().stream()
                .map(v -> VentaResponseDTO.from(v, getConversionSilent(v.getTotalPagado())))
                .toList();
    }

    @Override
    @Transactional
    public VentaResponseDTO registrarVenta(VentaRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", dto.clienteId()));

        Vehiculo vehiculo = vehiculoRepository.findByIdForUpdate(dto.vehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", dto.vehiculoId()));

        if (!vehiculo.estaDisponibleParaVenta()) {
            throw new BusinessRuleException(
                    "El vehículo %s no está disponible para la venta. Estado actual: %s"
                            .formatted(vehiculo.getPlaca(), vehiculo.getEstado())
            );
        }

        BigDecimal precioBase = vehiculo.getPrecio();
        BigDecimal porcDesc = BigDecimal.ZERO;
        BigDecimal montoDesc = BigDecimal.ZERO;

        if (precioBase.compareTo(umbralDescuento) > 0) {
            porcDesc = porcentajeDescuento;
            montoDesc = precioBase.multiply(porcDesc)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_EVEN);
            log.info("Aplicando descuento de {}% al vehículo {}. Descuento: {}", porcDesc, vehiculo.getPlaca(), montoDesc);
        }

        BigDecimal total = precioBase.subtract(montoDesc);

        Venta venta = Venta.builder()
                .cliente(cliente)
                .vehiculo(vehiculo)
                .precioBase(precioBase)
                .porcentajeDescuento(porcDesc)
                .montoDescuento(montoDesc)
                .totalPagado(total)
                .observaciones(dto.observaciones())
                .fechaVenta(LocalDateTime.now())
                .build();

        vehiculo.marcarComoVendido();
        
        venta = ventaRepository.save(venta);

        ConversionUsdDTO conversion = currencyExchangeService.convertirCopAUsd(total);

        return VentaResponseDTO.from(venta, conversion);
    }

    private ConversionUsdDTO getConversionSilent(BigDecimal monto) {
        try {
            return currencyExchangeService.convertirCopAUsd(monto);
        } catch (Exception e) {
            log.warn("Fallo al obtener conversión para lista de ventas: {}", e.getMessage());
            return null;
        }
    }
}
