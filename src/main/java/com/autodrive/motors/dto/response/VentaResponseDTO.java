package com.autodrive.motors.dto.response;

import com.autodrive.motors.entity.Cliente;
import com.autodrive.motors.entity.Vehiculo;
import com.autodrive.motors.entity.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Respuesta de una venta: desglose en COP + equivalente en USD.
 */
public record VentaResponseDTO(
        Long id,
        LocalDateTime fechaVenta,
        ClienteResumen cliente,
        VehiculoResumen vehiculo,
        BigDecimal precioBaseCop,
        BigDecimal porcentajeDescuento,
        BigDecimal descuentoAplicadoCop,
        BigDecimal totalPagadoCop,
        BigDecimal totalPagadoUsd,
        BigDecimal tasaCambioCopUsd,
        String fuenteTasaCambio,
        String observaciones
) {

    public record ClienteResumen(Long id, String cedula, String nombreCompleto, String email) {
        static ClienteResumen from(Cliente c) {
            return new ClienteResumen(c.getId(), c.getCedula(), c.getNombreCompleto(), c.getEmail());
        }
    }

    public record VehiculoResumen(Long id, String placa, String marca, String modelo, Integer anio) {
        static VehiculoResumen from(Vehiculo v) {
            return new VehiculoResumen(v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(), v.getAnio());
        }
    }

    /**
     * @param venta      entidad persistida (con cliente y vehículo cargados)
     * @param conversion conversión del total a USD; si es {@code null} los campos USD quedan vacíos
     */
    public static VentaResponseDTO from(Venta venta, ConversionUsdDTO conversion) {
        return new VentaResponseDTO(
                venta.getId(),
                venta.getFechaVenta(),
                ClienteResumen.from(venta.getCliente()),
                VehiculoResumen.from(venta.getVehiculo()),
                venta.getPrecioBase(),
                venta.getPorcentajeDescuento(),
                venta.getMontoDescuento(),
                venta.getTotalPagado(),
                conversion != null ? conversion.montoUsd() : null,
                conversion != null ? conversion.tasa() : null,
                conversion != null ? conversion.fuente().name() : null,
                venta.getObservaciones()
        );
    }
}
