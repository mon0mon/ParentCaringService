package com.lumanlab.parentcaringservice.logger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class RestClientLogger implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);

        ClientHttpResponse response = execution.execute(request, body);

        return logResponse(request, response);
    }

    private ClientHttpResponse logResponse(HttpRequest request, ClientHttpResponse response) throws IOException {
        log.trace("Response status: {}", response.getStatusCode());
        logHeaders(response.getHeaders());

        byte[] responseBody = response.getBody().readAllBytes();
        if (responseBody.length > 0) {
            log.trace("Response body: {}", new String(responseBody, StandardCharsets.UTF_8));
        }

        return new BufferingClientHttpResponseWrapper(response, responseBody);
    }

    private void logRequest(HttpRequest request, byte[] body) {
        log.trace("Request: {} {}", request.getMethod(), request.getURI());
        logHeaders(request.getHeaders());

        if (body != null && body.length > 0) {
            log.trace("Request body: {}", new String(body, StandardCharsets.UTF_8));
        }
    }

    private void logHeaders(HttpHeaders headers) {
        headers.forEach((name, values) -> {
            log.trace("Request header: {}={}", name, values);
        });
    }
}
