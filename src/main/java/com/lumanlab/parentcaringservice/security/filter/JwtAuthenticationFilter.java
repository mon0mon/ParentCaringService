package com.lumanlab.parentcaringservice.security.filter;

import com.lumanlab.parentcaringservice.impersonationlog.domain.ImpersonationType;
import com.lumanlab.parentcaringservice.impersonationlog.port.inp.UpdateImpersonationLog;
import com.lumanlab.parentcaringservice.security.ActionDetailsExtractor;
import com.lumanlab.parentcaringservice.security.domain.UserPrincipal;
import com.lumanlab.parentcaringservice.security.jwt.application.service.JwtTokenService;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.port.outp.UserRepository;
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
import java.util.*;
import java.util.stream.Collectors;

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
    private final UserRepository userRepository;
    private final UpdateImpersonationLog updateImpersonationLog;
    private final ActionDetailsExtractor actionDetailsExtractor;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (token != null) {
                Authentication authentication = authenticateToken(token);

                // 인증 객체가 존재하는 경우
                if (authentication != null) {
                    // Spring Security Context에 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

                    // 어드민 임퍼소네이션인 경우, 저장
                    if (principal.isImpersonation()) {
                        String ip = request.getRemoteAddr();
                        String actionDetails = actionDetailsExtractor.extractActionDetails(request);

                        updateImpersonationLog.register(
                                principal.impersonatorId(), principal.id(), ip, ImpersonationType.ACTION, actionDetails
                        );
                    }

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
            String userIdStr = claims.getSubject();

            if (!StringUtils.hasText(userIdStr)) {
                log.debug("JWT 토큰에 사용자 ID가 없습니다");

                return null;
            }

            Long userId = Long.parseLong(userIdStr);
            if (!userRepository.existsById(userId)) {
                log.debug("존재하지 않는 User ID 입니다");

                return null;
            }

            // JWT에서 UserRole Set을 추출
            Set<UserRole> userRoles = extractUserRoles(claims);

            // JWT에서 ImpersonatorId를 추출
            Long impersonatorId = claims.get("impersonatorId") == null ? null : ((Number) claims.get("impersonatorId")).longValue();

            // UserPrincipal 객체를 생성
            UserPrincipal principal = new UserPrincipal(userId, userRoles, impersonatorId);

            // UserRole Set을 기반으로 GrantedAuthority 컬렉션을 생성
            Collection<SimpleGrantedAuthority> authorities = createAuthoritiesFromRoles(userRoles);

            // 생성된 principal과 authorities로 Authentication 객체를 생성
            return new UsernamePasswordAuthenticationToken(principal, null, authorities);
        } catch (JwtException e) {
            log.debug("JWT 토큰 검증 실패: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * JWT 클레임에서 사용자의 역할 정보를 추출하여 UserRole 셋을 반환함
     *
     * @param claims JWT 클레임을 포함하는 객체
     * @return 추출된 UserRole 셋, 역할 정보가 없으면 기본 역할(PARENT)을 포함한 셋 반환
     */
    private Set<UserRole> extractUserRoles(Claims claims) {
        String rolesString = sanitizeRolesString((String) claims.get("roles"));

        if (!StringUtils.hasText(rolesString)) {
            // 역할 정보가 없으면 기본 역할(PARENT) 부여
            return Set.of(UserRole.PARENT);
        }

        return Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .map(roleName -> {
                    try {
                        return UserRole.valueOf(roleName);
                    } catch (IllegalArgumentException e) {
                        log.warn("유효하지 않은 사용자 역할: {}", roleName);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 유저 역할(Role) 셋을 기반으로 Spring Security에서 사용하는 권한(Authority) 정보 컬렉션을 생성합니다
     *
     * @param roles 유저 역할을 나타내는 UserRole 객체의 셋
     * @return 생성된 SimpleGrantedAuthority 컬렉션. 역할 셋이 없거나 null 일 경우 기본 역할(ROLE_PARENT)을 포함한 컬렉션 반환
     */
    private Collection<SimpleGrantedAuthority> createAuthoritiesFromRoles(Set<UserRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of(new SimpleGrantedAuthority("ROLE_PARENT"));
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

    private String sanitizeRolesString(String roles) {
        return roles.replaceAll("[\\[\\]]", "");
    }
}
