package groom.backend.common.redis;

import groom.backend.common.redis.dto.LocationMessageDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RedisPublisher {

  private final RedisTemplate<String, Object> template;

  public RedisPublisher(RedisTemplate<String, Object> template) {
    this.template = template;
  }

  /**
   * 사용자 UUID 기반 위치 데이터 publish
   */
  public void publishLocation(UUID userId, LocationMessageDto locationMessage) {
    String topic = "location:" + userId;
    template.convertAndSend(topic, locationMessage);
  }
}
