package groom.backend.common.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import groom.backend.domain.location.dto.request.LocationUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationPublisher {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> template;

    public void publishLocation(Long relationId, LocationUpdateRequest req) {
        String channel = "location:" + relationId;

        try {
            String message = objectMapper.writeValueAsString(req);

            template.convertAndSend(channel, message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("직렬화 실패", e);
        }

    }

}
