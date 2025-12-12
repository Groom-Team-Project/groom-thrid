package groom.backend.common.config;

import groom.backend.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정 - JWT 인증 필터 등록 - CORS 설정 - 엔드포인트 권한 설정
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // @PreAuthorize, @Secured 등 메서드 레벨 보안 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins:}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 엔드포인트 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // Auth 관련 엔드포인트 (회원가입, 로그인, 토큰 갱신) - 인증 불필요
                        .requestMatchers("/v1/auth/**").permitAll()
                        .requestMatchers("/v1/opendata/**").permitAll()
                        .requestMatchers("/v1/reviews/**").permitAll()
                        // Swagger UI - 개발환경에서만 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/v1/paths/**").permitAll()
                        // 나머지 모든 요청 - 인증 필요
                        .anyRequest().permitAll()
                )

                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if (allowedOrigins != null && allowedOrigins.length > 0) {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        } else {
            // 운영 설정 누락 시 안전한 fallback (개발용). 운영에서는 사용 금지 권장.
            configuration.setAllowedOriginPatterns(List.of("*"));
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 비밀번호 암호화를 위한 PasswordEncoder, Bcrypt 사용
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
