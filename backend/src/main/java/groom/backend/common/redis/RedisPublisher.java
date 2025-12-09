package groom.backend.common.redis;

import groom.backend.common.redis.dto.LocationMessageDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {

    private final RedisTemplate<String, Object> template;

    public RedisPublisher(RedisTemplate<String, Object> template) {
        this.template = template;
    }

    public void publishLocation(Long relationId, LocationMessageDto locationMessage) {
        String topic = "location:" + relationId;
        template.convertAndSend(topic, locationMessage);
    }
}
