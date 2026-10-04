package com.autodrive.motors.service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Respuesta de {@code GET https://open.er-api.com/v6/latest/{base}}.
 * <pre>
 * { "result": "success", "base_code": "COP",
 *   "time_last_update_unix": 1791072152, "rates": { "USD": 0.000302, ... } }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExchangeRateApiResponse(
        String result,
        @JsonProperty("base_code") String baseCode,
        @JsonProperty("time_last_update_unix") Long timeLastUpdateUnix,
        @JsonProperty("error-type") String errorType,
        Map<String, BigDecimal> rates
) {
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(result);
    }
}
