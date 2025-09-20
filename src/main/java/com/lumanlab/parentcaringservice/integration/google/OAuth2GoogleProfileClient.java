package com.lumanlab.parentcaringservice.integration.google;

import com.lumanlab.parentcaringservice.oauth2.OAuth2Properties;
import com.lumanlab.parentcaringservice.oauth2.port.outp.OAuth2Client;
import com.lumanlab.parentcaringservice.oauth2.port.outp.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class OAuth2GoogleProfileClient implements OAuth2Client {

    private final OAuth2Properties oAuth2Properties;
    private final RestClient restClient;

    @Override
    public UserProfileResponse requestProfile(String authorization) {
        String baseUrl = oAuth2Properties.getGoogleProperties().getProfile().getUrl();
        String path = oAuth2Properties.getGoogleProperties().getProfile().getPath();
        String url = baseUrl + path;

        GoogleProfileResponse response = restClient.get()
                .uri(url)
                .header("Authorization", authorization)
                .retrieve()
                .toEntity(GoogleProfileResponse.class)
                .getBody();

        return response.toUserProfileResponse();
    }
}
