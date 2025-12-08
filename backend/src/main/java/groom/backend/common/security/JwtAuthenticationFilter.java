package groom.backend.common.security;

import groom.backend.domain.users.entity.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            String token = extractTokenFromRequest(request);

            // 2. 토큰이 있고 유효한 경우
            if (token != null && jwtUtil.validateToken(token)) {

                // 3. 토큰에서 사용자 정보 추출
                AuthUser userInfo = jwtUtil.getUserInfoFromToken(token);

                UUID userId = userInfo.userId();
                Role role = userInfo.role();
                Long relationId = userInfo.relationId();

                // 4. Spring Security 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                new AuthUser(userId, role, relationId),
                                null,    // credentials (비밀번호는 불필요)
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                // 5. 요청 상세 정보 설정
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 6. SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공 - userId: {}, role: {}, relationId: {}", userId, role, relationId);
            }

        } catch (Exception e) {
            log.error("JWT 인증 실패: {}", e.getMessage());
            // 인증 실패 시 SecurityContext를 비워둠 (다음 필터에서 401 처리)
            SecurityContextHolder.clearContext();
        }

        // 7. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 Bearer 토큰만 추출
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 문자 제거
        }

        return null;
    }
}
