package groom.backend.common.redis;

import groom.backend.common.redis.dto.LocationMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, Object> template;

    public void publishLocation(Long relationId, LocationMessageDto locationMessage) {
        String topic = "location:" + relationId;
        template.convertAndSend(topic, locationMessage);
    }
}
