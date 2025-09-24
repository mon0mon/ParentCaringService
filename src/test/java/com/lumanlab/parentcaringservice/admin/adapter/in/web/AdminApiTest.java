package com.lumanlab.parentcaringservice.admin.adapter.in.web;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.lumanlab.parentcaringservice.support.BaseApiTest;
import com.lumanlab.parentcaringservice.support.annotation.WithMasterUser;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminApiTest extends BaseApiTest {

    @Test
    @WithMasterUser
    @DisplayName("사용자 임퍼소네이션")
    void impersonate() throws Exception {
        var targetUser = authHelper.createUser("user@example.com", "password", null, UserRole.PARENT);

        mockMvc.perform(withAuth(post("/api/admin/impersonate/{userId}", targetUser.getId())
                        .header("User-Agent", UserAgent.LUMANLAB_ADMIN))
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.accessToken").exists(),
                        jsonPath("$.refreshToken").exists(),
                        jsonPath("$.refreshTokenExpiredAt").exists()
                )
                .andDo(MockMvcRestDocumentationWrapper.document("admin-impersonation",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Admin")
                                .summary("어드민 임퍼소네이션")
                                .description("특정 유저의 인증 정보로 인증 토큰 발급")
                                .requestHeaders(
                                        headerWithName("User-Agent").description("사용자 에이전트 정보")
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
