package com.autodrive.motors.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;

@ConfigurationProperties(prefix = "exchange.api")
public record ExchangeApiProperties(
        String baseUrl,
        String latestPath,
        String baseCurrency,
        String targetCurrency,
        Duration connectTimeout,
        Duration readTimeout,
        BigDecimal fallbackRate,
        Duration cacheTtl
) {
}
