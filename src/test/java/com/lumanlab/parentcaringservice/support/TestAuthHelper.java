package com.lumanlab.parentcaringservice.support;

import com.lumanlab.parentcaringservice.security.jwt.application.service.JwtTokenService;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.port.outp.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@Transactional
public class TestAuthHelper {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenService jwtTokenService;

    /**
     * 테스트용 사용자 생성 및 JWT 토큰 반환
     */
    public String createUserAndGetToken(String email, String password, UserRole... roles) {
        User user = new User(email, passwordEncoder.encode(password), Set.of(roles));
        User savedUser = userRepository.save(user);

        return jwtTokenService.generateAccessToken(savedUser.getId(), null);
    }

    /**
     * 기본 테스트 사용자로 JWT 토큰 생성
     */
    public String createDefaultUserToken() {
        return createUserAndGetToken("test@example.com", "password", UserRole.PARENT);
    }

    /**
     * 부모 권한 사용자 토큰 생성
     */
    public String createParentUserToken() {
        return createUserAndGetToken("parent@example.com", "parent123", UserRole.PARENT);
    }

    /**
     * 관리자 권한 사용자 토큰 생성
     */
    public String createAdminUserToken() {
        return createUserAndGetToken("admin@example.com", "admin123", UserRole.ADMIN);
    }

    /**
     * 마스터 권한 사용자 토큰 생성
     */
    public String createMasterUserToken() {
        return createUserAndGetToken("master@example.com", "master123", UserRole.MASTER);
    }

    /**
     * Swagger UI용 고정 토큰 생성 (문서화용)
     * 실제 운영에서는 사용하지 말 것
     */
    public String createDocumentationToken() {
        return createUserAndGetToken("swagger@example.com", "swagger123", UserRole.PARENT);
    }

    /**
     * 문서화를 위한 간소화된 토큰 생성
     */
    public String getSimpleTokenForDocs() {
        // 간단한 예시 토큰 (실제 JWT가 아닌 문서화용)
        return "your-jwt-token-here";
    }

    /**
     * MockHttpServletRequestBuilder에 Authorization 헤더 추가
     */
    public MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }
}
