package com.autodrive.motors.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    public static final String EXCHANGE_REST_CLIENT = "exchangeRestClient";

    @Bean(EXCHANGE_REST_CLIENT)
    public RestClient exchangeRestClient(RestClient.Builder builder, ExchangeApiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        factory.setReadTimeout((int) properties.readTimeout().toMillis());

        return builder
                .requestFactory(factory)
                .build();
    }
}
