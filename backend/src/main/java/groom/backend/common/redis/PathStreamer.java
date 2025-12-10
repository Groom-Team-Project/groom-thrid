package groom.backend.common.redis;

import groom.backend.domain.path.dto.request.PathFindRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PathStreamer {

    private final RedisTemplate<String, Object> redisTemplate;

    // 네비게이션 시작 시 위치 정보를 Stream에 추가
    public void publish(Long relationId, PathFindRequest req) {

        // redis steam 키 셋팅
        String streamKey = "location-stream:" + relationId;

        Map<String, Object> fields = new HashMap<>();

        fields.put("startX", req.getStartX());
        fields.put("startY", req.getStartY());
        fields.put("startName", req.getStartName());
        fields.put("endX", req.getEndX());
        fields.put("endY", req.getEndY());
        fields.put("endName", req.getEndName());

        RecordId recordId = redisTemplate.opsForStream().add(
                StreamRecords.mapBacked(fields)
                        .withStreamKey(streamKey)
        );

        log.info("Location stream published. stream={}, recordId={}",
                streamKey, recordId);
    }

    // 목적지 도달 시 해당 relationId의 Stream 데이터 삭제
    public void delete(Long relationId) {
        String streamKey = "location-stream:" + relationId;

        boolean deleted = redisTemplate.delete(streamKey);

        if (deleted) {
            log.info("Location stream deleted. stream={}", streamKey);
        } else {
            log.warn("Location stream not found or already deleted. stream={}", streamKey);
        }
    }
}
