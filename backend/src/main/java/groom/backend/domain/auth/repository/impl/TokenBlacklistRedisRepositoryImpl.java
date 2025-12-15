package groom.backend.domain.auth.repository.impl;

import groom.backend.domain.auth.repository.spec.TokenBlacklistRedisRepository;
import groom.backend.domain.auth.vo.BlacklistTokenValue;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TokenBlacklistRedisRepositoryImpl implements TokenBlacklistRedisRepository {

    // Redis Key 접두사
    private static final String BLACKLIST_ACCESS_PREFIX = "blacklist:access:";
    private static final String BLACKLIST_USER_VERSION_PREFIX = "blacklist:user:";
    private static final String VERSION_SUFFIX = ":version";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Override
    public void save(String token, BlacklistTokenValue value, long ttlMillis) {
        String key = BLACKLIST_ACCESS_PREFIX + token;
        redisTemplate.opsForValue().set(key, value, ttlMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean existsByToken(String token) {
        String key = BLACKLIST_ACCESS_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public Optional<BlacklistTokenValue> findByToken(String token) {
        String key = BLACKLIST_ACCESS_PREFIX + token;
        Object value = redisTemplate.opsForValue().get(key);

        if (value instanceof BlacklistTokenValue) {
            return Optional.of((BlacklistTokenValue) value);
        }

        return Optional.empty();
    }

    @Override
    public Long getUserBlacklistVersion(UUID userId) {
        String key = BLACKLIST_USER_VERSION_PREFIX + userId + VERSION_SUFFIX;
        Object value = redisTemplate.opsForValue().get(key);

        if (value != null) {
            return ((Number) value).longValue();
        }

        return 0L;
    }

    @Override
    public Long incrementUserBlacklistVersion(UUID userId) {
        String key = BLACKLIST_USER_VERSION_PREFIX + userId + VERSION_SUFFIX;
        Long newVersion = redisTemplate.opsForValue().increment(key);

        // Access Token 만료 시간만큼 TTL 설정
        redisTemplate.expire(key, accessTokenExpiration, TimeUnit.MILLISECONDS);

        return newVersion;
    }
}
