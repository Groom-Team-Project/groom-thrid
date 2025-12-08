package groom.backend.common.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import groom.backend.common.redis.dto.LocationMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

/**
 * Redis 구독 데이터를 처리하는 Listener
 * (사용자의 위치 업데이트 시 호출됨)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscribeListener implements MessageListener {

  private final ObjectMapper objectMapper;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      String json = new String(message.getBody());
      // publishLocation() 에서 보낸 DTO로 역직렬화
      LocationMessageDto dto = objectMapper.readValue(json, LocationMessageDto.class);

      log.info("SUB | User: {} | Current({}, {}) → Dest({}, {})",
              dto.getUserId(), dto.getCurrentX(), dto.getCurrentY(),
              dto.getDestX(), dto.getDestY());

      // TODO: 위치 기반 업데이트 로직 수행
      // ex) DB 저장 / 경로 안내 서비스 호출 / 웹소켓으로 프론트에 push

    } catch (JsonProcessingException e) {
      log.error("JSON parse error in Redis subscriber: {}", e.getMessage());
    }
  }
}