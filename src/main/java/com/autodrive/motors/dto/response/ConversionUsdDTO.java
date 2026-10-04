package com.autodrive.motors.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resultado de convertir un monto COP a USD.
 */
public record ConversionUsdDTO(
        BigDecimal montoCop,
        BigDecimal tasa,
        BigDecimal montoUsd,
        FuenteTasa fuente,
        LocalDateTime fechaTasa
) {
    public enum FuenteTasa {
        /** Tasa obtenida de la API externa (en vivo o desde caché vigente). */
        API,
        /** La API falló: se usó la última tasa conocida o la tasa configurada. */
        RESPALDO
    }
}
