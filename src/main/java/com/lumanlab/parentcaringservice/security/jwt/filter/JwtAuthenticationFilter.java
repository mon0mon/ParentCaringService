package com.lumanlab.parentcaringservice.security.jwt.filter;

import com.lumanlab.parentcaringservice.security.jwt.application.service.JwtTokenService;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JWT 인증 필터
 * <p>
 * HTTP 요청의 Authorization 헤더에서 Bearer JWT 토큰을 추출하여 검증
 * 유효한 토큰인 경우 Spring Security Context에 인증 정보를 설정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (token != null) {
                Authentication authentication = authenticateToken(token);
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT 토큰 인증 성공: {}", authentication.getName());
                }
            }
        } catch (JwtException e) {
            log.debug("JWT 토큰 인증 실패: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            log.error("JWT 인증 필터에서 예외 발생", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰을 추출
     *
     * @param request HTTP 요청 객체
     * @return 추출된 JWT 토큰 문자열, 토큰이 없거나 형식이 잘못된 경우 null
     */
    private String extractToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * JWT 토큰을 검증하고 인증 객체를 생성
     *
     * @param token JWT 토큰 문자열
     * @return 생성된 인증 객체, 토큰이 유효하지 않은 경우 null
     */
    private Authentication authenticateToken(String token) {
        try {
            Claims claims = jwtTokenService.validateJwtToken(token);

            String userId = claims.getSubject();
            if (!StringUtils.hasText(userId)) {
                log.debug("JWT 토큰에 사용자 ID가 없습니다");
                return null;
            }

            Collection<SimpleGrantedAuthority> authorities = extractAuthorities(claims);

            // 사용자 ID와 권한 정보로 Authentication 객체 생성
            return new UsernamePasswordAuthenticationToken(userId, null, authorities);

        } catch (JwtException e) {
            log.debug("JWT 토큰 검증 실패: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * JWT 클레임에서 사용자 권한 정보를 추출
     *
     * @param claims JWT 클레임 정보
     * @return 사용자 권한 목록
     */
    private Collection<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // JWT 클레임에서 roles 정보 추출
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?> rolesList) {
            for (Object roleObj : rolesList) {
                if (roleObj instanceof String roleStr) {
                    try {
                        UserRole userRole = UserRole.valueOf(roleStr);
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.name()));
                    } catch (IllegalArgumentException e) {
                        log.warn("유효하지 않은 사용자 역할: {}", roleStr);
                    }
                }
            }
        }

        // 기본 권한이 없는 경우 기본 사용자 권한 부여
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }
}
