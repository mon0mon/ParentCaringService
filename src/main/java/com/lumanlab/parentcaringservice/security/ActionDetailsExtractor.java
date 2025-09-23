package com.lumanlab.parentcaringservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ActionDetailsExtractor {

    private final ObjectMapper objectMapper;

    public String extractActionDetails(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;

        String method = wrapper.getMethod();
        String endpoint = wrapper.getRequestURI();
        String payload = new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8);

        ObjectNode detailsNode = objectMapper.createObjectNode();
        detailsNode.put("method", method);
        detailsNode.put("endpoint", endpoint);

        try {
            if (!payload.isEmpty()) {
                detailsNode.set("payload", objectMapper.readTree(payload));
            } else {
                detailsNode.putNull("payload");
            }
        } catch (IOException e) {
            detailsNode.put("payload", payload);
        }

        return detailsNode.toString();
    }
}
