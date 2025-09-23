package com.lumanlab.parentcaringservice.me.port.inp.web;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshToken;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenProvider;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenRepository;
import com.lumanlab.parentcaringservice.support.BaseApiTest;
import com.lumanlab.parentcaringservice.support.annotation.WithTestUser;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.Map;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MeApiTest extends BaseApiTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenProvider refreshTokenProvider;

    @Test
    @WithTestUser
    @DisplayName("세션 목록 조회")
    void querySessions() throws Exception {
        User currentUser = getCurrentUser();
        var refreshTokenDto =
                refreshTokenProvider.generateRefreshToken(currentUser.getId(), Map.of("roles", currentUser.getRoles()));

        refreshTokenRepository.save(
                new RefreshToken(
                        currentUser, refreshTokenDto.tokenHash(), "127.0.0.1", UserAgent.MOBILE, OffsetDateTime.now(),
                        OffsetDateTime.now().plusDays(1)
                )
        );

        mockMvc.perform(withAuth(get("/api/me/sessions")))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.sessions").isArray(),
                        jsonPath("$.sessions.*.id").exists(),
                        jsonPath("$.sessions.*.ip").exists(),
                        jsonPath("$.sessions.*.issuedAt").exists(),
                        jsonPath("$.sessions.*.expiredAt").exists()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("session-gets",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Session")
                                .summary("현재 로그인한 사용자의 세션 목록 조회")
                                .description("활성화된 RefreshToken의 목록을 조회")
                                .responseFields(
                                        fieldWithPath("sessions").description("세션 정보 배열"),
                                        fieldWithPath("sessions[].id").description("RefreshToken ID"),
                                        fieldWithPath("sessions[].ip").description("RefreshToken 발급 시점의 IP"),
                                        fieldWithPath("sessions[].issuedAt").description(
                                                "RefreshToken 발급 시간 (Unix Timestamp)"),
                                        fieldWithPath("sessions[].expiredAt").description(
                                                "RefreshToken 만료 시간 (Unix Timestamp)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @WithTestUser
    @DisplayName("세션 삭제")
    void revokeSession() throws Exception {
        User currentUser = getCurrentUser();
        var refreshTokenDto =
                refreshTokenProvider.generateRefreshToken(currentUser.getId(), Map.of("roles", currentUser.getRoles()));

        RefreshToken refreshToken = refreshTokenRepository.save(
                new RefreshToken(
                        currentUser, refreshTokenDto.tokenHash(), "127.0.0.1", UserAgent.MOBILE, OffsetDateTime.now(),
                        OffsetDateTime.now().plusDays(1)
                )
        );

        mockMvc.perform(withAuth(delete("/api/me/sessions/{refreshTokenId}", refreshToken.getId())))
                .andExpectAll(
                        status().isOk()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("session-revoke",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Session")
                                .summary("주어진 Session Id를 만료 처리")
                                .description("주어진 Session Id를 만료 처리")
                                .build()
                        )
                ));
    }
}
