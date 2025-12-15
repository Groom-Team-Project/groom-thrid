package groom.backend.domain.auth.service.impl;

import groom.backend.common.security.AuthUser;
import groom.backend.common.security.JwtUtil;
import groom.backend.domain.auth.enums.BlacklistReason;
import groom.backend.domain.auth.repository.spec.TokenBlacklistRedisRepository;
import groom.backend.domain.auth.service.spec.TokenBlacklistService;
import groom.backend.domain.auth.vo.BlacklistTokenValue;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final TokenBlacklistRedisRepository blacklistRepository;
    private final JwtUtil jwtUtil;

    @Override
    public boolean isBlacklisted(String token) {
        // 1. 개별 토큰 블랙리스트 확인
        if (blacklistRepository.existsByToken(token)) {
            log.warn("Token found in blacklist: {}", token.substring(0, Math.min(20, token.length())) + "...");
            return true;
        }

        // 2. 사용자 버전 확인 (비밀번호 변경 시나리오)
        if (!isUserVersionValid(token)) {
            log.warn("Token user version mismatch (password changed)");
            return true;
        }

        return false;
    }

    @Override
    public void blacklistToken(String token, UUID userId, BlacklistReason reason) {
        // 토큰의 남은 TTL 계산
        long ttl = calculateRemainingTTL(token);

        if (ttl <= 0) {
            log.debug("Token already expired, skipping blacklist");
            return;
        }

        BlacklistTokenValue value = BlacklistTokenValue.of(userId, reason);
        blacklistRepository.save(token, value, ttl);

        log.info("Token blacklisted - userId: {}, reason: {}, ttl: {}ms",
                userId, reason, ttl);
    }

    @Override
    public void blacklistAllUserTokens(UUID userId, BlacklistReason reason) {
        // 사용자 블랙리스트 버전 증가
        Long newVersion = blacklistRepository.incrementUserBlacklistVersion(userId);

        log.info("All tokens invalidated for userId: {}, reason: {}, newVersion: {}",
                userId, reason, newVersion);
    }

    @Override
    public boolean isUserVersionValid(String token) {
        try {
            AuthUser authUser = jwtUtil.getUserInfoFromToken(token);
            UUID userId = authUser.userId();

            // 사용자의 현재 블랙리스트 버전 조회
            Long currentVersion = blacklistRepository.getUserBlacklistVersion(userId);

            // 버전 0 = 비밀번호 변경 없음, 모든 토큰 유효
            // 버전 > 0 = 비밀번호 변경됨, 기존 토큰 무효
            return currentVersion == 0L;

        } catch (Exception e) {
            log.error("Failed to validate user version: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰의 남은 TTL 계산 (만료까지 남은 시간)
     */
    private long calculateRemainingTTL(String token) {
        try {
            Claims claims = jwtUtil.parseToken(token);
            long expiresAt = claims.getExpiration().getTime();
            long now = System.currentTimeMillis();
            return expiresAt - now;
        } catch (Exception e) {
            log.error("Failed to calculate TTL: {}", e.getMessage());
            return 0L;
        }
    }
}
