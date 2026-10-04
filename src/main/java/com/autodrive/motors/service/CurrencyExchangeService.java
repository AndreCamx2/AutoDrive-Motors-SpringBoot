package com.autodrive.motors.service;

import com.autodrive.motors.dto.response.ConversionUsdDTO;

import java.math.BigDecimal;

/**
 * Conversión de montos COP → USD usando una API externa de tasas de cambio.
 */
public interface CurrencyExchangeService {

    /**
     * Convierte un monto en COP a USD (2 decimales).
     * Nunca falla por indisponibilidad de la API: en ese caso usa la tasa de respaldo
     * e informa {@link ConversionUsdDTO.FuenteTasa#RESPALDO}.
     *
     * @throws IllegalArgumentException si el monto es nulo o negativo
     */
    ConversionUsdDTO convertirCopAUsd(BigDecimal montoCop);
}
