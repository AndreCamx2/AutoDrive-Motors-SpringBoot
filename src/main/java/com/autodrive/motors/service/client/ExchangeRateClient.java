package com.autodrive.motors.service.client;

import com.autodrive.motors.config.ExchangeApiProperties;
import com.autodrive.motors.config.RestClientConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class ExchangeRateClient {

    private final RestClient restClient;
    private final ExchangeApiProperties props;

    public ExchangeRateClient(@Qualifier(RestClientConfig.EXCHANGE_REST_CLIENT) RestClient restClient,
                              ExchangeApiProperties props) {
        this.restClient = restClient;
        this.props = props;
    }

    public ExchangeRateApiResponse getLatestRates() {
        URI uri = UriComponentsBuilder.fromHttpUrl(props.baseUrl())
                .path(props.latestPath())
                .buildAndExpand(props.baseCurrency())
                .toUri();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(ExchangeRateApiResponse.class);
    }
}
