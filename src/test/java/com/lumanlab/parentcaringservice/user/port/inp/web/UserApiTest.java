package com.lumanlab.parentcaringservice.user.port.inp.web;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.lumanlab.parentcaringservice.support.BaseApiTest;
import com.lumanlab.parentcaringservice.support.annotation.WithTestUser;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.port.inp.web.view.req.LoginUserViewReq;
import com.lumanlab.parentcaringservice.user.port.inp.web.view.req.RegisterUserViewReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
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
                ));
    }

    @Test
    @DisplayName("사용자 회원가입")
    void registerUser() throws Exception {
        var req = new RegisterUserViewReq("newuser@example.com", "newpassword123", null);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .header("User-Agent", UserAgent.MOBILE)
                )
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
                                        fieldWithPath("password").description("사용자 비밀번호"),
                                        fieldWithPath("totpSecret").description(
                                                        "MFA TOTP 비밀키 (ADMIN, MASTER 권한 유저에게는 필수 값)")
                                                .optional()
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("사용자 회원가입 - SuperUser 회원가입 시, TOTP가 누락되면 예외 발생")
    void registerSuperUserThrowException() throws Exception {
        var req = new RegisterUserViewReq("newuser@example.com", "newpassword123", null);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .header("User-Agent", UserAgent.LUMANLAB_ADMIN))
                .andExpectAll(
                        status().isBadRequest()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("register-user-TOTP-missing",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 회원가입 - TOTP 에러")
                                .description("SuperUser 회원가입 시, TOTP가 누락되면 예외 발생")
                                .requestFields(
                                        fieldWithPath("email").description("사용자 이메일"),
                                        fieldWithPath("password").description("사용자 비밀번호"),
                                        fieldWithPath("totpSecret").description(
                                                        "MFA TOTP 비밀키 (ADMIN, MASTER 권한 유저에게는 필수 값)")
                                                .optional()
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("사용자 로그인")
    void loginUser() throws Exception {
        authHelper.createUserAndGetToken("login@example.com", "password123", UserRole.PARENT);

        var req = new LoginUserViewReq("login@example.com", "password123");

        mockMvc.perform(post("/api/users/login")
                        .header("User-Agent", UserAgent.MOBILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
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
                ));
    }
}
