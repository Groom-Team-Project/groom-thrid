package groom.backend.domain.auth.repository.impl;

import groom.backend.domain.auth.repository.spec.RefreshTokenRedisRepository;
import groom.backend.domain.auth.vo.RefreshTokenValue;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepositoryImpl implements RefreshTokenRedisRepository {

    // Redis Key 접두사
    private static final String KEY_PREFIX = "refresh_token:";
    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // RefreshToken 저장
    @Override
    public void save(String token, RefreshTokenValue value) {
        String key = KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, value, refreshTokenExpiration, TimeUnit.MILLISECONDS);
    }

    // RefreshToken 조회
    @Override
    public Optional<RefreshTokenValue> findByToken(String token) {
        String key = KEY_PREFIX + token;
        Object value = redisTemplate.opsForValue().get(key);

        if (value instanceof RefreshTokenValue) {
            return Optional.of((RefreshTokenValue) value);
        }

        return Optional.empty();
    }

    // RefreshToken 존재 여부 확인
    @Override
    public boolean existsByToken(String token) {
        String key = KEY_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // RefreshToken 삭제
    @Override
    public void deleteByToken(String token) {
        String key = KEY_PREFIX + token;
        redisTemplate.delete(key);
    }
}
