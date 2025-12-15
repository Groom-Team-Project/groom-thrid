package groom.backend.domain.auth.service.spec;

import groom.backend.domain.auth.enums.BlacklistReason;
import java.util.UUID;

public interface TokenBlacklistService {

    // 토큰이 블랙리스트에 있는지 확인
    boolean isBlacklisted(String token);

    // 단일 토큰을 블랙리스트에 추가 (로그아웃, 강제 로그아웃)
    void blacklistToken(String token, UUID userId, BlacklistReason reason);

    // 사용자의 모든 토큰 무효화 (비밀번호 변경 시)
    void blacklistAllUserTokens(UUID userId, BlacklistReason reason);

    // 토큰의 사용자 버전이 유효한지 확인
    boolean isUserVersionValid(String token);
}
