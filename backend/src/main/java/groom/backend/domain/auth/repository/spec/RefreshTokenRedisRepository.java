package groom.backend.domain.auth.repository.spec;

import groom.backend.domain.auth.vo.RefreshTokenValue;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRedisRepository {

    void save(String token, RefreshTokenValue value);

    Optional<RefreshTokenValue> findByToken(String token);

    boolean existsByToken(String token);

    void deleteByToken(String token);

    // 특정 사용자의 모든 Refresh Token 삭제 (비밀번호 변경, 강제 로그아웃 시 사용)
    void deleteAllByUserId(UUID userId);
}
