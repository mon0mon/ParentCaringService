package com.lumanlab.parentcaringservice.oauth2;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {

    private GoogleProperties google;

    @Data
    public static class GoogleProperties {
        private GoogleProfileProperties profile;
    }

    @Data
    public static class GoogleProfileProperties {
        private String url;
        private String path;
    }
}
