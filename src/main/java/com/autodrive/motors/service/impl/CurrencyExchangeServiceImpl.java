package com.autodrive.motors.service.impl;

import com.autodrive.motors.config.ExchangeApiProperties;
import com.autodrive.motors.dto.response.ConversionUsdDTO;
import com.autodrive.motors.exception.ExternalServiceException;
import com.autodrive.motors.service.CurrencyExchangeService;
import com.autodrive.motors.service.client.ExchangeRateApiResponse;
import com.autodrive.motors.service.client.ExchangeRateClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {

    private final ExchangeRateClient exchangeRateClient;
    private final ExchangeApiProperties props;

    private final AtomicReference<CachedRate> cachedRate = new AtomicReference<>();
    private final Lock updateLock = new ReentrantLock();

    @Override
    public ConversionUsdDTO convertirCopAUsd(BigDecimal montoCop) {
        CachedRate currentRateInfo = cachedRate.get();
        LocalDateTime rateTimestamp = (currentRateInfo != null) ? currentRateInfo.timestamp() : LocalDateTime.now();
        BigDecimal tasa = obtenerTasaCambio();
        
        ConversionUsdDTO.FuenteTasa fuente = tasa.equals(props.fallbackRate()) 
                ? ConversionUsdDTO.FuenteTasa.RESPALDO 
                : ConversionUsdDTO.FuenteTasa.API;

        BigDecimal equivalenteUsd = montoCop.multiply(tasa)
                .setScale(2, RoundingMode.HALF_EVEN);

        return new ConversionUsdDTO(montoCop, tasa, equivalenteUsd, fuente, rateTimestamp);
    }

    private BigDecimal obtenerTasaCambio() {
        CachedRate current = cachedRate.get();
        if (current != null && current.isValid(props.cacheTtl().toMinutes())) {
            return current.rate;
        }

        if (updateLock.tryLock()) {
            try {
                current = cachedRate.get();
                if (current != null && current.isValid(props.cacheTtl().toMinutes())) {
                    return current.rate;
                }

                BigDecimal newRate = fetchRateFromApi();
                cachedRate.set(new CachedRate(newRate, LocalDateTime.now()));
                return newRate;

            } catch (Exception e) {
                log.error("Fallo al actualizar tasa desde API. Usando tasa anterior si existe, o el fallback.", e);
                current = cachedRate.get();
                if (current != null) {
                    log.warn("Usando tasa caché expirada: {}", current.rate);
                    return current.rate;
                }
                log.warn("Usando fallback configurado: {}", props.fallbackRate());
                return props.fallbackRate();
            } finally {
                updateLock.unlock();
            }
        }

        current = cachedRate.get();
        if (current != null) {
            return current.rate;
        }

        return props.fallbackRate();
    }

    private BigDecimal fetchRateFromApi() {
        ExchangeRateApiResponse response = exchangeRateClient.getLatestRates();
        if (response == null || !response.isSuccess()) {
            throw new ExternalServiceException("Respuesta inválida de la API de divisas");
        }

        Map<String, BigDecimal> rates = response.rates();
        if (rates == null || !rates.containsKey(props.targetCurrency())) {
            throw new ExternalServiceException("No se encontró la tasa para " + props.targetCurrency());
        }

        return rates.get(props.targetCurrency());
    }

    private record CachedRate(BigDecimal rate, LocalDateTime timestamp) {
        public boolean isValid(long ttlMinutes) {
            return LocalDateTime.now().isBefore(timestamp.plusMinutes(ttlMinutes));
        }
    }
}
