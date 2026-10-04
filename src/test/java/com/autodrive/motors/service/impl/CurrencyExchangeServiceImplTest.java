package com.autodrive.motors.service.impl;

import com.autodrive.motors.config.ExchangeApiProperties;
import com.autodrive.motors.dto.response.ConversionUsdDTO;
import com.autodrive.motors.service.client.ExchangeRateApiResponse;
import com.autodrive.motors.service.client.ExchangeRateClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyExchangeServiceImplTest {

    @Mock
    private ExchangeRateClient client;

    @Mock
    private ExchangeApiProperties props;

    @InjectMocks
    private CurrencyExchangeServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(props.fallbackRate()).thenReturn(new BigDecimal("0.00020"));
        lenient().when(props.cacheTtl()).thenReturn(Duration.ofMinutes(30));
        lenient().when(props.targetCurrency()).thenReturn("USD");
    }

    @Test
    void fallbackRateSeUsaSiApiFalla() {
        when(client.getLatestRates()).thenThrow(new RuntimeException("Network Error"));
        ConversionUsdDTO result = service.convertirCopAUsd(new BigDecimal("1000000"));
        assertThat(result.fuente()).isEqualTo(ConversionUsdDTO.FuenteTasa.RESPALDO);
        assertThat(result.tasa()).isEqualByComparingTo(new BigDecimal("0.00020"));
    }

    @Test
    void apiRateSeUsaYGuardaEnCache() {
        ExchangeRateApiResponse resp = new ExchangeRateApiResponse(
                "success", "COP", 1600000000L, null, Map.of("USD", new BigDecimal("0.00025"))
        );
        when(client.getLatestRates()).thenReturn(resp);

        ConversionUsdDTO result = service.convertirCopAUsd(new BigDecimal("1000000"));
        assertThat(result.fuente()).isEqualTo(ConversionUsdDTO.FuenteTasa.API);
        assertThat(result.tasa()).isEqualByComparingTo(new BigDecimal("0.00025"));
    }
}
