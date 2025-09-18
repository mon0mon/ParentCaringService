package com.lumanlab.parentcaringservice.user.port.inp.web;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.lumanlab.parentcaringservice.support.BaseApiTest;
import com.lumanlab.parentcaringservice.support.annotation.WithTestUser;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserApiTest extends BaseApiTest {

    @Test
    @WithTestUser
    @DisplayName("사용자 프로필 조회")
    void getUserProfile() throws Exception {
        mockMvc.perform(withAuth(get("/api/users/profile"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.userId").exists(),
                        jsonPath("$.email").value("test@example.com"),
                        jsonPath("$.status").value("ACTIVE"),
                        jsonPath("$.role").isArray(),
                        jsonPath("$.mfaEnabled").isBoolean()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("get-user-profile",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 프로필 조회")
                                .description("인증된 사용자의 프로필 정보를 조회합니다")
                                .responseFields(
                                        fieldWithPath("userId").description("사용자 ID"),
                                        fieldWithPath("email").description("사용자 이메일"),
                                        fieldWithPath("status").description("사용자 상태"),
                                        fieldWithPath("role").description("사용자 권한 목록"),
                                        fieldWithPath("mfaEnabled").description("다단계 인증 활성화 여부")
                                )
                                .build()
                        )
                ))
                .andDo(document("get-user-profile",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer JWT 토큰")
                        ),
                        responseFields(
                                fieldWithPath("userId").description("사용자 ID"),
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("status").description("사용자 상태"),
                                fieldWithPath("role").description("사용자 권한 목록"),
                                fieldWithPath("mfaEnabled").description("다단계 인증 활성화 여부")
                        )
                ));
    }

    @Test
    @DisplayName("사용자 회원가입")
    void registerUser() throws Exception {
        String requestBody = """
                {
                    "email": "newuser@example.com",
                    "password": "newpassword123"
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpectAll(
                        status().isOk()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("register-user",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 회원가입")
                                .description("새로운 사용자를 등록합니다")
                                .requestFields(
                                        fieldWithPath("email").description("사용자 이메일"),
                                        fieldWithPath("password").description("사용자 비밀번호")
                                )
                                .build()
                        )
                ))
                .andDo(document("register-user",
                        requestFields(
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("password").description("사용자 비밀번호")
                        )
                ));
    }

    @Test
    @DisplayName("사용자 로그인")
    void loginUser() throws Exception {
        authHelper.createUserAndGetToken("login@example.com", "password123", UserRole.PARENT);

        String requestBody = """
                {
                    "email": "login@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/users/login")
                        .header("User-Agent", "Test-Browser/1.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.accessToken").exists(),
                        jsonPath("$.refreshToken").exists()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("login-user",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 로그인")
                                .description("사용자 인증을 진행하고 JWT 토큰을 발급받습니다")
                                .requestHeaders(
                                        headerWithName("User-Agent").description("사용자 에이전트 정보")
                                )
                                .requestFields(
                                        fieldWithPath("email").description("사용자 이메일"),
                                        fieldWithPath("password").description("사용자 비밀번호")
                                )
                                .responseFields(
                                        fieldWithPath("accessToken").description("액세스 토큰"),
                                        fieldWithPath("refreshToken").description("리프레시 토큰"),
                                        fieldWithPath("refreshTokenExpiredAt").description("리프레시 토큰 만료 시간")
                                )
                                .build()
                        )
                ))
                .andDo(document("login-user",
                        requestHeaders(
                                headerWithName("User-Agent").description("사용자 에이전트 정보")
                        ),
                        requestFields(
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("password").description("사용자 비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").description("액세스 토큰"),
                                fieldWithPath("refreshToken").description("리프레시 토큰"),
                                fieldWithPath("refreshTokenExpiredAt").description("리프레시 토큰 만료 시간")
                        )
                ));
    }
}
