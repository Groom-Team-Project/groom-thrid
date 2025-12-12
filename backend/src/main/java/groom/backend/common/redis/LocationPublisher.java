package groom.backend.common.redis;

import groom.backend.domain.location.dto.request.LocationUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationPublisher {

    private final RedisTemplate<String, Object> template;

    public void publishLocation(Long relationId, LocationUpdateRequest req) {
        String channel = "location:" + relationId;

        // RedisTemplate이 GenericJackson2JsonRedisSerializer를 사용하므로
        // 객체를 직접 전달하면 자동으로 JSON으로 직렬화됨
        template.convertAndSend(channel, req);
    }

}
