package com.lumanlab.parentcaringservice.config;

import com.lumanlab.parentcaringservice.security.jwt.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final AuthenticationFailureHandler oAuth2FailureHandler;
    private final OAuth2AuthorizedClientService jdbcOAuth2AuthorizedClientService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Swagger 관련
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        // 정적 리소스
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/img/**", "/oauth2.html",
                                "/oauth2-response.html")
                        .permitAll()
                        // OAuth2
                        .requestMatchers("/oauth2/authorization/**", "/oauth2/code/**")
                        .permitAll()
                        // JWK 키 조회
                        .requestMatchers(HttpMethod.GET, "/.well-known/jwks.json")
                        .permitAll()
                        // 공개 API (인증 불필요)
                        .requestMatchers(HttpMethod.POST, "/api/users/register", "/api/users/login",
                                "/api/users/login/oauth2", "/api/token/refresh", "/api/totp",
                                "/api/users/totp/verify")
                        .permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll() // CORS preflight 요청 허용
                        // 나머지는 인증 필요
                        .anyRequest()
                        .authenticated())
                .oauth2Login(oauth2 ->
                        oauth2.successHandler(oAuth2SuccessHandler)
                                .failureHandler(oAuth2FailureHandler)
                                .authorizedClientService(jdbcOAuth2AuthorizedClientService)
                )
        ;

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 오리진 설정
        configuration.setAllowedOriginPatterns(List.of("http://localhost:8080", "http://localhost:63342"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용할 헤더
        configuration.setAllowedHeaders(
                Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin",
                        "Access-Control-Request-Method", "Access-Control-Request-Headers", "X-CSRF-TOKEN",
                        "Cache-Control", "User-Agent", "X-Forwarded-For", "Forwarded", "referer", "Sec-Ch-Ua",
                        "Sec-Ch-Ua-Mobile", "Sec-Ch-Ua-Platform"));

        // 노출할 헤더 (클라이언트에서 접근 가능한 헤더)
        configuration.setExposedHeaders(List.of("Authorization"));

        // 자격증명(쿠키, 인증 헤더 등) 포함 허용
        configuration.setAllowCredentials(true);

        // preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
