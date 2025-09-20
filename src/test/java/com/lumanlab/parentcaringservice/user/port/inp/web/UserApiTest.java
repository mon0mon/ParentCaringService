package com.lumanlab.parentcaringservice.user.port.inp.web;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.lumanlab.parentcaringservice.support.BaseApiTest;
import com.lumanlab.parentcaringservice.support.annotation.WithTestUser;
import com.lumanlab.parentcaringservice.totp.application.service.NonceService;
import com.lumanlab.parentcaringservice.totp.application.service.TotpProvider;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.port.inp.web.view.req.LoginUserViewReq;
import com.lumanlab.parentcaringservice.user.port.inp.web.view.req.RegisterUserViewReq;
import com.lumanlab.parentcaringservice.user.port.inp.web.view.req.UpdateUserTotpViewReq;
import com.lumanlab.parentcaringservice.user.port.inp.web.view.req.VerifyUserTotpViewReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserApiTest extends BaseApiTest {

    @Autowired
    private NonceService nonceService;

    @MockitoSpyBean
    private TotpProvider totpProvider;

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
        var req = new RegisterUserViewReq("newuser@example.com", "newpassword123");

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
                                        fieldWithPath("password").description("사용자 비밀번호")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("사용자 로그인")
    void loginUser() throws Exception {
        authHelper.createUserAndGetToken("login@example.com", "password123", null, UserRole.PARENT);

        var req = new LoginUserViewReq("login@example.com", "password123");

        mockMvc.perform(post("/api/users/login")
                        .header("User-Agent", UserAgent.MOBILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.accessToken").exists(),
                        jsonPath("$.refreshToken").exists(),
                        jsonPath("$.refreshTokenExpiredAt").exists()
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

    @Test
    @DisplayName("사용자 로그인 - MFA 초기화가 필요한 경우 예외 발생")
    void loginUserMfaInitializeRequireException() throws Exception {
        authHelper.createUserAndGetToken("login@example.com", "password123", null, UserRole.MASTER);

        var req = new LoginUserViewReq("login@example.com", "password123");

        mockMvc.perform(post("/api/users/login")
                        .header("User-Agent", UserAgent.LUMANLAB_ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpectAll(
                        status().isPreconditionRequired(),
                        jsonPath("$.additionalData.nonce").exists()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("login-user-mfa-initialize-require-exception",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 로그인 - MFA 등록 예외")
                                .description("ADMIN, MASTER 사용자는 MFA 설정이 안되어있는 경우, MFA 등록 예외 발생")
                                .requestHeaders(
                                        headerWithName("User-Agent").description("사용자 에이전트 정보")
                                )
                                .requestFields(
                                        fieldWithPath("email").description("사용자 이메일"),
                                        fieldWithPath("password").description("사용자 비밀번호")
                                )
                                .responseFields(
                                        fieldWithPath("errorCode").description("에러 코드"),
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("timestamp").description("에러 발생 시각 (UTC 기준 시간)"),
                                        subsectionWithPath("additionalData").description("에러 추가 정보").optional()
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("사용자 로그인 - MFA 인증이 필요한 경우 예외 발생")
    void loginUserMfaVerificationRequireException() throws Exception {
        authHelper.createUserAndGetToken("login@example.com", "password123", "TOTP_SECRET", UserRole.MASTER);

        var req = new LoginUserViewReq("login@example.com", "password123");

        mockMvc.perform(post("/api/users/login")
                        .header("User-Agent", UserAgent.LUMANLAB_ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpectAll(
                        status().isPreconditionRequired(),
                        jsonPath("$.additionalData.nonce").exists()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("login-user-mfa-verification-require-exception",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 로그인 - MFA 인증 필요 예외")
                                .description("MFA 설정이 true인 사용자는, MFA 인증 코드를 추가로 인증해야함")
                                .requestHeaders(
                                        headerWithName("User-Agent").description("사용자 에이전트 정보")
                                )
                                .requestFields(
                                        fieldWithPath("email").description("사용자 이메일"),
                                        fieldWithPath("password").description("사용자 비밀번호")
                                )
                                .responseFields(
                                        fieldWithPath("errorCode").description("에러 코드"),
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("timestamp").description("에러 발생 시각 (UTC 기준 시간)"),
                                        subsectionWithPath("additionalData").description("에러 추가 정보").optional()
                                )
                                .build()
                        )
                ));
    }

    @Test
    @WithTestUser
    @DisplayName("사용자 TOTP 업데이트")
    void updateUserTotp() throws Exception {
        final String NEW_TOTP_SECRET = "NEW_TOTP_SECRET";

        var req = new UpdateUserTotpViewReq(NEW_TOTP_SECRET);

        mockMvc.perform(withAuth(post("/api/users/totp"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                )
                .andExpectAll(
                        status().isOk()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("update-user-totp",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 TOTP 업데이트")
                                .description("현재 로그인한 유저의 TOTP를 업데이트합니다")
                                .requestFields(
                                        fieldWithPath("totpSecret").description(
                                                "MFA TOTP 비밀키")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @WithTestUser
    @DisplayName("사용자 TOTP 삭제")
    void clearUserTotp() throws Exception {
        mockMvc.perform(withAuth(delete("/api/users/totp")))
                .andExpectAll(
                        status().isOk()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("clear-user-totp",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 TOTP 삭제")
                                .description("현재 로그인한 유저의 TOTP를 삭제합니다")
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("사용자 TOTP 인증")
    void userTotpVerification() throws Exception {
        User user = authHelper.createUser("login@example.com", "password123", "TOTP_SECRET", UserRole.PARENT);
        String nonce = nonceService.generateNonce(user.getId());

        doReturn(true).when(totpProvider).verifyTotp(any(String.class), any(Integer.class));

        var req = new VerifyUserTotpViewReq(nonce, 123456);

        mockMvc.perform((post("/api/users/totp/verify"))
                        .header("User-Agent", UserAgent.MOBILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.accessToken").exists(),
                        jsonPath("$.refreshToken").exists(),
                        jsonPath("$.refreshTokenExpiredAt").exists()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("verify-user-totp",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 TOTP 인증 코드 검증")
                                .description("MFA 설정이 true인 사용자들을 대상으로, TOTP 인증 코드를 검증")
                                .requestFields(
                                        fieldWithPath("nonce").description("사용자를 증빙할 임시 nonce"),
                                        fieldWithPath("verificationCode").description("TOTP 인증 코드")
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

    @Test
    @DisplayName("사용자 TOTP 인증 - MFA 인증이 false 인 경우 예외")
    void userTotpVerificationMfaEnabledFalseThrowException() throws Exception {
        User user = authHelper.createUser("login@example.com", "password123", null, UserRole.PARENT);
        String nonce = nonceService.generateNonce(user.getId());

        var req = new VerifyUserTotpViewReq(nonce, 123456);

        mockMvc.perform((post("/api/users/totp/verify"))
                        .header("User-Agent", UserAgent.MOBILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                )
                .andExpectAll(
                        status().is4xxClientError()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("verify-user-totp-mfa-enabled-false-throw-exception",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 TOTP 인증 - MFA 설정이 False인 경우 예외")
                                .description("MFA 설정이 false인 사용자는 예외 발생")
                                .requestFields(
                                        fieldWithPath("nonce").description("사용자를 증빙할 임시 nonce"),
                                        fieldWithPath("verificationCode").description("TOTP 인증 코드")
                                )
                                .responseFields(
                                        fieldWithPath("errorCode").description("에러 코드"),
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("timestamp").description("에러 발생 시각 (UTC 기준 시간)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("사용자 TOTP 인증 - 인증 코드 검증이 실패한 경우 예외 발생")
    void userTotpVerificationVerificationCodeFailToVerifyThrowException() throws Exception {
        User user = authHelper.createUser("login@example.com", "password123", "TOTP_SECRET", UserRole.PARENT);
        String nonce = nonceService.generateNonce(user.getId());

        doReturn(false).when(totpProvider).verifyTotp(any(String.class), any(Integer.class));

        var req = new VerifyUserTotpViewReq(nonce, 123456);

        mockMvc.perform((post("/api/users/totp/verify"))
                        .header("User-Agent", UserAgent.MOBILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                )
                .andExpectAll(
                        status().isUnauthorized()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("verify-user-totp",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 TOTP 인증 코드 검증")
                                .description("MFA 설정이 true인 사용자들을 대상으로, TOTP 인증 코드를 검증")
                                .requestFields(
                                        fieldWithPath("nonce").description("사용자를 증빙할 임시 nonce"),
                                        fieldWithPath("verificationCode").description("TOTP 인증 코드")
                                )
                                .responseFields(
                                        fieldWithPath("errorCode").description("에러 코드"),
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("timestamp").description("에러 발생 시각 (UTC 기준 시간)")
                                )
                                .build()
                        )
                ));
    }
}
