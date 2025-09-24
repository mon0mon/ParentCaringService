package com.lumanlab.parentcaringservice.config;

import com.lumanlab.parentcaringservice.logger.RestClientLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class RestClientConfig {

    private final RestClientLogger restClientLogger;

    @Bean
    public RestClient restClient() {
        return RestClient.builder().requestInterceptor(restClientLogger).build();
    }
}
