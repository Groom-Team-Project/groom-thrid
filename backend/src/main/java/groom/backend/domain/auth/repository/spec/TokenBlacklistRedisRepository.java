package groom.backend.domain.auth.repository.spec;

import groom.backend.domain.auth.vo.BlacklistTokenValue;
import java.util.Optional;
import java.util.UUID;

public interface TokenBlacklistRedisRepository {

    // Access Token을 블랙리스트에 추가
    void save(String token, BlacklistTokenValue value, long ttlMillis);

    // 토큰이 블랙리스트에 존재하는지 확인
    boolean existsByToken(String token);

    // 블랙리스트 토큰 정보 조회
    Optional<BlacklistTokenValue> findByToken(String token);

    // 사용자 블랙리스트 버전 조회 (비밀번호 변경 추적용)
    Long getUserBlacklistVersion(UUID userId);

    // 사용자 블랙리스트 버전 증가 (비밀번호 변경 시 호출)
    Long incrementUserBlacklistVersion(UUID userId);
}
