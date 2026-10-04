package com.autodrive.motors.service.client;

import com.autodrive.motors.config.ExchangeApiProperties;
import com.autodrive.motors.config.RestClientConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(ExchangeRateClient.class)
@Import(RestClientConfig.class)
@EnableConfigurationProperties(ExchangeApiProperties.class)
class ExchangeRateClientTest {

    @Autowired
    private ExchangeRateClient client;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getLatestRatesDevuelveRespuestaCorrecta() throws Exception {
        ExchangeRateApiResponse mockResponse = new ExchangeRateApiResponse(
                "success", "COP", 1600000000L, null, Map.of("USD", new BigDecimal("0.00025"))
        );

        server.expect(requestTo("https://open.er-api.com/v6/latest/COP"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        ExchangeRateApiResponse response = client.getLatestRates();

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        org.junit.jupiter.api.Assertions.assertTrue(response.rates().get("USD").doubleValue() > 0);
    }
}
