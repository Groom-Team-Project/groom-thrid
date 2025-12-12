package groom.backend.domain.auth.repository.spec;

import groom.backend.domain.auth.vo.RefreshTokenValue;
import java.util.Optional;

public interface RefreshTokenRedisRepository {

    void save(String token, RefreshTokenValue value);

    Optional<RefreshTokenValue> findByToken(String token);

    boolean existsByToken(String token);

    void deleteByToken(String token);
}
