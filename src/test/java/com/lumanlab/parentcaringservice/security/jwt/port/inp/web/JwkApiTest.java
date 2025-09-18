package com.lumanlab.parentcaringservice.security.jwt.port.inp.web;

import com.lumanlab.parentcaringservice.security.jwt.application.service.JwkManager;
import com.lumanlab.parentcaringservice.support.BaseApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class JwkApiTest extends BaseApiTest {

    @MockitoBean
    private JwkManager jwkManager;

    private Map<String, Object> mockJwkSet;

    @BeforeEach
    void setUp() {
        // Mock JWK Set 데이터 준비
        Map<String, Object> jwk1 =
                Map.of("kty", "RSA", "use", "sig", "alg", "RS256", "kid", "key-123456789", "n", "mockModulus1", "e",
                        "AQAB");

        Map<String, Object> jwk2 =
                Map.of("kty", "RSA", "use", "sig", "alg", "RS256", "kid", "key-987654321", "n", "mockModulus2", "e",
                        "AQAB");

        mockJwkSet = Map.of("keys", List.of(jwk1, jwk2));
    }

    @Test
    @DisplayName("JWK Set 조회 - 성공")
    void getJwks_Success() throws Exception {
        // Given
        given(jwkManager.getPublicJwkSet()).willReturn(mockJwkSet);

        // When & Then
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andDo(print())
                .andExpectAll(status().isOk(), content().contentType("application/json;charset=UTF-8"),
                        jsonPath("$.keys").isArray(), jsonPath("$.keys.length()").value(2),
                        jsonPath("$.keys[0].kty").value("RSA"), jsonPath("$.keys[0].use").value("sig"),
                        jsonPath("$.keys[0].alg").value("RS256"), jsonPath("$.keys[0].kid").value("key-123456789"),
                        jsonPath("$.keys[0].n").value("mockModulus1"), jsonPath("$.keys[0].e").value("AQAB"),
                        jsonPath("$.keys[1].kty").value("RSA"), jsonPath("$.keys[1].use").value("sig"),
                        jsonPath("$.keys[1].alg").value("RS256"), jsonPath("$.keys[1].kid").value("key-987654321"),
                        jsonPath("$.keys[1].n").value("mockModulus2"), jsonPath("$.keys[1].e").value("AQAB"));

        // Verify
        verify(jwkManager).getPublicJwkSet();
    }

    @Test
    @DisplayName("JWK Set 조회 - 빈 키 목록")
    void getJwks_EmptyKeys() throws Exception {
        // Given
        Map<String, Object> emptyJwkSet = Map.of("keys", List.of());
        given(jwkManager.getPublicJwkSet()).willReturn(emptyJwkSet);

        // When & Then
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andDo(print())
                .andExpectAll(status().isOk(), content().contentType("application/json;charset=UTF-8"),
                        jsonPath("$.keys").isArray(), jsonPath("$.keys.length()").value(0));

        // Verify
        verify(jwkManager).getPublicJwkSet();
    }

    @Test
    @DisplayName("JWK Set 조회 - 단일 키")
    void getJwks_SingleKey() throws Exception {
        // Given
        Map<String, Object> singleJwk =
                Map.of("kty", "RSA", "use", "sig", "alg", "RS256", "kid", "key-single", "n", "singleModulus", "e",
                        "AQAB");
        Map<String, Object> singleKeyJwkSet = Map.of("keys", List.of(singleJwk));
        given(jwkManager.getPublicJwkSet()).willReturn(singleKeyJwkSet);

        // When & Then
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andDo(print())
                .andExpectAll(status().isOk(), content().contentType("application/json;charset=UTF-8"),
                        jsonPath("$.keys").isArray(), jsonPath("$.keys.length()").value(1),
                        jsonPath("$.keys[0].kty").value("RSA"), jsonPath("$.keys[0].use").value("sig"),
                        jsonPath("$.keys[0].alg").value("RS256"), jsonPath("$.keys[0].kid").value("key-single"),
                        jsonPath("$.keys[0].n").value("singleModulus"), jsonPath("$.keys[0].e").value("AQAB"));


        // Verify
        verify(jwkManager).getPublicJwkSet();
    }

    @Test
    @DisplayName("JWK Set 조회 - 서비스 예외 발생")
    void getJwks_ServiceException() throws Exception {
        // Given
        given(jwkManager.getPublicJwkSet()).willThrow(new RuntimeException("키 생성 중 오류 발생"));

        // When & Then
        mockMvc.perform(get("/.well-known/jwks.json")).andDo(print()).andExpect(status().isInternalServerError());

        // Verify
        verify(jwkManager).getPublicJwkSet();
    }

    @Test
    @DisplayName("JWK Set 조회 - 잘못된 엔드포인트")
    void getJwks_WrongEndpoint() throws Exception {
        // When & Then
        mockMvc.perform(get("/.well-known/jwks")).andDo(print()).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("JWK Set 조회 - POST 메소드 허용하지 않음")
    void getJwks_MethodNotAllowed() throws Exception {
        // When & Then
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/.well-known/jwks" + ".json"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("JWK Set 조회 - Content-Type 검증")
    void getJwks_ContentType() throws Exception {
        // Given
        given(jwkManager.getPublicJwkSet()).willReturn(mockJwkSet);

        // When & Then
        mockMvc.perform(get("/.well-known/jwks.json").accept("application/json"))
                .andDo(print())
                .andExpectAll(status().isOk(), header().string("Content-Type", "application/json;charset=UTF-8"));

        // Verify
        verify(jwkManager).getPublicJwkSet();
    }

    @Test
    @DisplayName("JWK Set 조회 - 표준 JWK 형식 검증")
    void getJwks_StandardJwkFormat() throws Exception {
        // Given
        given(jwkManager.getPublicJwkSet()).willReturn(mockJwkSet);

        // When & Then
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andDo(print())
                .andExpectAll(status().isOk(), content().contentType("application/json;charset=UTF-8"))
                // JWK Set 구조 검증
                .andExpectAll(jsonPath("$.keys").exists(), jsonPath("$.keys").isArray())
                // 각 JWK 필수 필드 검증
                .andExpectAll(jsonPath("$.keys[*].kty").exists(), jsonPath("$.keys[*].use").exists(),
                        jsonPath("$.keys[*].alg").exists(), jsonPath("$.keys[*].kid").exists(),
                        jsonPath("$.keys[*].n").exists(), jsonPath("$.keys[*].e").exists());

        // Verify
        verify(jwkManager).getPublicJwkSet();
    }
}
