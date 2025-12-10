package groom.backend.common.security;

import groom.backend.domain.users.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // Access Token 생성 (userId + role + relationId 포함)
    public String generateAccessToken(UUID userId, Role role, String name, Long relationId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())  // RBAC을 위한 role 추가
                .claim("name", name)  // 사용자 이름 추가
                .claim("relationId", relationId) // relationId 추가 없으면 null
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성 (userId만 포함)
    public String generateRefreshToken(UUID userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 userId, role, relationId 추출
    public AuthUser getUserInfoFromToken(String token) {
        Claims claims = parseToken(token);

        // 타입 직렬화
        UUID userId = UUID.fromString(claims.getSubject());
        Role role = Role.valueOf(claims.get("role", String.class));
        Long relationId = claims.get("relationId") != null
                ? ((Number) claims.get("relationId")).longValue()
                : null;

        return new AuthUser(userId, role, relationId);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // 토큰 파싱
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
