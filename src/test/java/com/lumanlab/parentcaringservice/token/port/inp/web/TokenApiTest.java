package com.lumanlab.parentcaringservice.token.port.inp.web;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshToken;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenDto;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenProvider;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenRepository;
import com.lumanlab.parentcaringservice.support.BaseApiTest;
import com.lumanlab.parentcaringservice.support.annotation.WithTestUser;
import com.lumanlab.parentcaringservice.token.adapter.in.web.view.req.RefreshAccessTokenViewReq;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;
import java.util.Map;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TokenApiTest extends BaseApiTest {

    final UserAgent USER_AGENT = UserAgent.MOBILE;
    @Autowired
    RefreshTokenProvider refreshTokenProvider;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Test
    @WithTestUser
    @DisplayName("액세스 토큰 갱신")
    void refreshAccessToken() throws Exception {
        // 테스트용 사용자 생성 및 리프레시 토큰 생성
        String userEmail = "token@example.com";
        String password = "password123";
        User user = authHelper.createUser(userEmail, password, null, UserRole.PARENT);

        // 테스트용 리프레시 토큰 생성
        RefreshTokenDto refreshTokenDto =
                refreshTokenProvider.generateRefreshToken(user.getId(), Map.of("role", user.getRolesString()));
        refreshTokenRepository.save(
                new RefreshToken(user, refreshTokenDto.tokenHash(), "IP", USER_AGENT, OffsetDateTime.now(),
                        OffsetDateTime.now().plusDays(1)));

        RefreshAccessTokenViewReq req = new RefreshAccessTokenViewReq(refreshTokenDto.token());

        mockMvc.perform(post("/api/token/refresh").header("User-Agent", USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpectAll(status().isOk(), jsonPath("$.accessToken").exists(), jsonPath("$.refreshToken").exists(),
                        jsonPath("$.refreshTokenExpiredAt").exists())
                .andDo(MockMvcRestDocumentationWrapper.document("refresh-access-token", resource(
                        ResourceSnippetParameters.builder()
                                .tag("Token")
                                .summary("액세스 토큰 갱신")
                                .description("리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급받습니다")
                                .requestHeaders(headerWithName("User-Agent").description("사용자 에이전트 정보"))
                                .requestFields(fieldWithPath("refreshToken").description("갱신할 리프레시 토큰"))
                                .responseFields(fieldWithPath("accessToken").description("새로 발급된 액세스 토큰"),
                                        fieldWithPath("refreshToken").description("새로 발급된 리프레시 토큰"),
                                        fieldWithPath("refreshTokenExpiredAt").description("새 리프레시 토큰의 만료 시간"))
                                .build())))
                .andDo(document("refresh-access-token",
                        requestHeaders(headerWithName("User-Agent").description("사용자 에이전트 정보")),
                        requestFields(fieldWithPath("refreshToken").description("갱신할 리프레시 토큰")),
                        responseFields(fieldWithPath("accessToken").description("새로 발급된 액세스 토큰"),
                                fieldWithPath("refreshToken").description("새로 발급된 리프레시 토큰"),
                                fieldWithPath("refreshTokenExpiredAt").description("새 리프레시 토큰의 만료 시간"))));
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 갱신 시도")
    void refreshAccessTokenWithInvalidToken() throws Exception {
        String requestBody = """
                {
                    "refreshToken": "invalid.refresh.token"
                }
                """;

        mockMvc.perform(post("/api/token/refresh").header("User-Agent", USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpectAll(status().is4xxClientError())
                .andDo(MockMvcRestDocumentationWrapper.document("refresh-access-token-invalid", resource(
                        ResourceSnippetParameters.builder()
                                .tag("Token")
                                .summary("유효하지 않은 리프레시 토큰으로 갱신 시도")
                                .description("유효하지 않은 리프레시 토큰을 사용할 경우 400 Bad Request 응답을 받습니다")
                                .requestHeaders(headerWithName("User-Agent").description("사용자 에이전트 정보"))
                                .requestFields(fieldWithPath("refreshToken").description("유효하지 않은 리프레시 토큰"))
                                .build())))
                .andDo(document("refresh-access-token-invalid",
                        requestHeaders(headerWithName("User-Agent").description("사용자 에이전트 정보")),
                        requestFields(fieldWithPath("refreshToken").description("유효하지 않은 리프레시 토큰"))));
    }

    @Test
    @DisplayName("만료된 리프레시 토큰으로 갱신 시도")
    void refreshAccessTokenWithExpiredToken() throws Exception {
        // 만료된 토큰 시뮬레이션 (실제로는 만료된 JWT 토큰을 생성해야 함)
        String expiredToken = "expired.refresh.token";

        String requestBody = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(expiredToken);

        mockMvc.perform(post("/api/token/refresh").header("User-Agent", USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpectAll(status().isUnauthorized())
                .andDo(MockMvcRestDocumentationWrapper.document("refresh-access-token-expired", resource(
                        ResourceSnippetParameters.builder()
                                .tag("Token")
                                .summary("만료된 리프레시 토큰으로 갱신 시도")
                                .description("만료된 리프레시 토큰을 사용할 경우 401 Unauthorized 응답을 받습니다")
                                .requestHeaders(headerWithName("User-Agent").description("사용자 에이전트 정보"))
                                .requestFields(fieldWithPath("refreshToken").description("만료된 리프레시 토큰"))
                                .build())))
                .andDo(document("refresh-access-token-expired",
                        requestHeaders(headerWithName("User-Agent").description("사용자 에이전트 정보")),
                        requestFields(fieldWithPath("refreshToken").description("만료된 리프레시 토큰"))));
    }
}
