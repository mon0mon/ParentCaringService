package com.lumanlab.parentcaringservice.user.port.inp.web;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.lumanlab.parentcaringservice.integration.google.OAuth2GoogleProfileClient;
import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Link;
import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Provider;
import com.lumanlab.parentcaringservice.oauth2.port.outp.OAuth2LinkRepository;
import com.lumanlab.parentcaringservice.oauth2.port.outp.UserProfileResponse;
import com.lumanlab.parentcaringservice.support.BaseApiTest;
import com.lumanlab.parentcaringservice.support.annotation.WithTestUser;
import com.lumanlab.parentcaringservice.totp.application.service.NonceService;
import com.lumanlab.parentcaringservice.totp.application.service.TotpProvider;
import com.lumanlab.parentcaringservice.user.adapter.in.web.view.req.*;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
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

    @Autowired
    private OAuth2LinkRepository oAuth2LinkRepository;

    @MockitoSpyBean
    private TotpProvider totpProvider;

    @MockitoBean
    private OAuth2GoogleProfileClient oAuth2GoogleProfileClient;

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
                        jsonPath("$.roles").isArray(),
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
                                        fieldWithPath("roles").description("사용자 권한 목록"),
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
    @DisplayName("사용자 로그인 - 유저 역할과 일치하지 않는 UserAgent일 경우 예외 발생")
    void loginUserNotMatchingUserRoleThrowException() throws Exception {
        authHelper.createUserAndGetToken("login@example.com", "password123", null, UserRole.MASTER);

        var req = new LoginUserViewReq("login@example.com", "password123");

        mockMvc.perform(post("/api/users/login")
                        .header("User-Agent", UserAgent.MOBILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpectAll(
                        status().is4xxClientError()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("login-user-not-matching-user-roles-throw-exception",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 로그인 - 유저 역할과 일치하지 않은 UserAgent는 예외")
                                .description(
                                        "UserRole과 UserAgent(PARENT - MOBILE, ADMIN - PARTNER_ADMIN, MASTER - " +
                                                "LUMANLAB_ADMIN)가 일치해야 함")
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
                                        fieldWithPath("timestamp").description("에러 발생 시각 (UTC 기준 시간)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("사용자 로그인 - 유저 역할과 일치하지 않는 UserAgent일 경우 예외 발생")
    void loginUserAuthorizationFailedThrowException() throws Exception {
        authHelper.createUserAndGetToken("login@example.com", "password123", null, UserRole.MASTER);

        var req = new LoginUserViewReq("login@example.com", "1234");

        mockMvc.perform(post("/api/users/login")
                        .header("User-Agent", UserAgent.MOBILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpectAll(
                        status().is4xxClientError()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("login-user-authorization-failed-throw-exception",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 로그인 - 유저 인증에 실패한 경우 예외 발생")
                                .description(
                                        "잘못된 비밀번호 또는 이메일로 로그인 시도")
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
                                        fieldWithPath("timestamp").description("에러 발생 시각 (UTC 기준 시간)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("사용자 로그인 - 이미 삭제된 유저의 경우 예외 처리")
    void loginUserUserStatusNotActiveException() throws Exception {
        User user = authHelper.createUser("login@example.com", "password123", null, UserRole.MASTER);
        user.withdraw();

        var req = new LoginUserViewReq("login@example.com", "password123");

        mockMvc.perform(post("/api/users/login")
                        .header("User-Agent", UserAgent.MOBILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpectAll(
                        status().is4xxClientError()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("login-user-user-status-not-active-exception",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 로그인 - 활성 상태가 아닌 유저로 로그인 시도 시 예외 발생")
                                .description(
                                        "활성 상태가 아닌 유저로 로그인 시도 시 예외 발생")
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
                                        fieldWithPath("timestamp").description("에러 발생 시각 (UTC 기준 시간)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("사용자 OAuth2 로그인")
    void oAuth2LoginUser() throws Exception {
        User user = authHelper.createUser("login@example.com", "password123", null, UserRole.PARENT);
        OAuth2Link oAuth2Link = oAuth2LinkRepository.save(new OAuth2Link(user, OAuth2Provider.GOOGLE, "OAUTH2_ID"));
        user.addOAuth2Link(oAuth2Link);

        doReturn(new UserProfileResponse(OAuth2Provider.GOOGLE, "OAUTH2_ID", "user@example.com", "user", 300L))
                .when(oAuth2GoogleProfileClient).requestProfile(any(String.class));

        var req = new OAuth2LoginViewReq("OAUTH2_ACCESS_TOKEN", OAuth2Provider.GOOGLE);

        mockMvc.perform(post("/api/users/login/oauth2")
                        .header("User-Agent", UserAgent.MOBILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.accessToken").exists(),
                        jsonPath("$.refreshToken").exists(),
                        jsonPath("$.refreshTokenExpiredAt").exists()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("login-oauth2-user",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 OAuth2 로그인")
                                .description("연동한 OAuth2로 사용자 인증을 진행하고 JWT 토큰을 발급받습니다")
                                .requestHeaders(
                                        headerWithName("User-Agent").description("사용자 에이전트 정보")
                                )
                                .requestFields(
                                        fieldWithPath("accessToken").description("OAuth2 제공자에게 받은 엑세스 토큰"),
                                        fieldWithPath("provider").description("OAuth2 제공자")
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
    @WithTestUser
    @DisplayName("사용자 TOTP 업데이트")
    void updateUserTotp() throws Exception {
        mockMvc.perform(withAuth(post("/api/users/totp")))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("image").exists()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("update-user-totp",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 TOTP 업데이트")
                                .description("현재 로그인한 유저의 TOTP를 업데이트합니다")
                                .responseFields(
                                        fieldWithPath("image").description("TOTP QR 코드 이미지")
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

    @Test
    @WithTestUser
    @DisplayName("사용자 OAuth2 연동")
    void userLinkOAuth2() throws Exception {
        doReturn(new UserProfileResponse(OAuth2Provider.GOOGLE, "OAUTH2_ID", "user@example.com", "user", 300L))
                .when(oAuth2GoogleProfileClient).requestProfile(any(String.class));

        var req = new LinkOAuth2ViewReq("OAUTH2_ACCESS_TOKEN");

        mockMvc.perform(withAuth(post("/api/users/oauth2-link/{provider}", OAuth2Provider.GOOGLE.name()))
                        .header("User-Agent", UserAgent.MOBILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                )
                .andExpectAll(
                        status().isOk()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("user-link-oauth2",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 OAuth2 연동")
                                .description("사용자 OAuth2 제공자 연동")
                                .pathParameters(
                                        parameterWithName("provider").description("OAuth2 제공자")
                                )
                                .requestFields(
                                        fieldWithPath("oAuth2AccessToken").description("외부 프로필을 조회할 OAuth2 액세스 토큰")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @WithTestUser
    @DisplayName("사용자 OAuth2 연동 제거")
    void userUnlinkOAuth2() throws Exception {
        User user = getCurrentUser();
        OAuth2Link oAuth2Link = oAuth2LinkRepository.save(new OAuth2Link(user, OAuth2Provider.GOOGLE, "OAUTH2_ID"));
        user.addOAuth2Link(oAuth2Link);

        mockMvc.perform(withAuth(delete("/api/users/oauth2-link/{provider}", OAuth2Provider.GOOGLE.name()))
                        .header("User-Agent", UserAgent.MOBILE)
                )
                .andExpectAll(
                        status().isOk()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("user-unlink-oauth2",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("사용자 OAuth2 연동 제거")
                                .description("사용자 OAuth2 제공자 연동 제거")
                                .pathParameters(
                                        parameterWithName("provider").description("OAuth2 제공자")
                                )
                                .build()
                        )
                ));
    }
}
