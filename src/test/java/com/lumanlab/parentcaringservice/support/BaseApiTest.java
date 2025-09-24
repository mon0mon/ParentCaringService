package com.lumanlab.parentcaringservice.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumanlab.parentcaringservice.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.restdocs.RestDocsAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@Transactional
@Import(RestDocsAutoConfiguration.class)
@ExtendWith({RestDocumentationExtension.class, TestUserExtension.class})
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class BaseApiTest {

    protected MockMvc mockMvc;

    @Autowired
    protected TestAuthHelper authHelper;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private User currentUser;
    private String currentUserToken;

    @BeforeEach
    void setUp(WebApplicationContext context, RestDocumentationContextProvider provider) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()) // Spring Security 필터 체인 적용
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider)
                        .operationPreprocessors()
                        .withRequestDefaults(Preprocessors.prettyPrint())
                        .withResponseDefaults(Preprocessors.prettyPrint()))
                .alwaysDo(MockMvcResultHandlers.print())
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    // 특정 토큰으로 인증 헤더 추가
    protected MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    // 현재 사용자 반환
    protected User getCurrentUser() {
        return currentUser;
    }

    // Extension에서 사용자를 설정하기 위한 메서드
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // 현재 사용자 토큰 반환
    protected String getCurrentUserToken() {
        return currentUserToken;
    }

    // Extension에서 토큰을 설정하기 위한 메서드
    public void setCurrentUserToken(String token) {
        this.currentUserToken = token;
    }

    // 문서화용 간소화된 인증 헤더 추가
    protected MockHttpServletRequestBuilder withAuthForDocs(MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer {jwt-token}");
    }

    // 현재 사용자 토큰으로 인증 헤더 추가
    protected MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder request) {
        if (currentUserToken == null) {
            throw new IllegalStateException("No user token available. Use @WithTestUser or similar annotations.");
        }
        return request.header("Authorization", "Bearer " + currentUserToken);
    }
}
