package com.lumanlab.parentcaringservice.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 인증이 성공했을 때 처리하는 핸들러
 */
@Slf4j
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();

        // 인증처리가 완료된 경우, 완료 처리
        log.debug("User {} is authenticated", authentication.getName());

        response.sendRedirect(
                String.format("/oauth2-response.html?status=success&provider=%s&oAuth2Id=%s&accessToken=%s",
                        principal.provider(),
                        principal.getName(), principal.accessToken()));
    }
}
