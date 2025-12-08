package groom.backend.domain.location.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import groom.backend.domain.location.dto.request.LocationUpdateRequest;
import groom.backend.domain.location.dto.response.LocationData;
import groom.backend.domain.location.service.spec.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 사용자의 위치 정보를 Redis에 발행 - 채널: location:{relationId} - 메시지: LocationData (JSON)
    @Override
    public void updateLocation(Long relationId, LocationUpdateRequest req) {

        LocationData locationData = LocationData.of(req);

        // Redis 채널명: location:{relationId}
        String channel = "location:" + relationId;

        try {
            // LocationData를 JSON으로 변환
            String message = objectMapper.writeValueAsString(locationData);

            // Redis에 발행
            redisTemplate.convertAndSend(channel, message);

            log.info("위치 정보 발행 완료: relationId={}, channel={}, lat={}, lng={}",
                    relationId, channel, req.lat(), req.lng());

        } catch (JsonProcessingException e) {
            log.error("위치 정보 JSON 변환 실패: userId={}, error={}", relationId, e.getMessage());
            throw new RuntimeException("위치 정보 전송 실패", e);
        }
    }
}
