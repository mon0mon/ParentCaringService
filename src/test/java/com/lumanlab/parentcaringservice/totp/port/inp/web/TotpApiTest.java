package com.lumanlab.parentcaringservice.totp.port.inp.web;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.lumanlab.parentcaringservice.support.BaseApiTest;
import com.lumanlab.parentcaringservice.totp.adapter.in.web.view.req.GenerateTotpViewReq;
import com.lumanlab.parentcaringservice.totp.application.service.NonceService;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TotpApiTest extends BaseApiTest {

    @Autowired
    private NonceService nonceService;

    @Test
    @DisplayName("Totp 등록")
    void loginUser() throws Exception {
        User user = authHelper.createUser("login@example.com", "password123", null, UserRole.PARENT);
        String nonce = nonceService.generateNonce(user.getId());

        var req = new GenerateTotpViewReq(nonce);

        mockMvc.perform(post("/api/totp")
                        .header("User-Agent", UserAgent.MOBILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.image").exists()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("generate-totp",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Totp")
                                .summary("회원가입 후, TOTP 등록")
                                .description("회원가입 후, TOTP 등록")
                                .requestHeaders(
                                        headerWithName("User-Agent").description("사용자 에이전트 정보")
                                )
                                .requestFields(
                                        fieldWithPath("nonce").description("사용자를 증빙할 임시 nonce")
                                )
                                .responseFields(
                                        fieldWithPath("image").description("TOTP QR 코드 이미지")
                                )
                                .build()
                        )
                ));
    }
}
