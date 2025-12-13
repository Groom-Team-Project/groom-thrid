package groom.backend.common.redis;

import groom.backend.domain.path.dto.request.PathFindRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
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

        // Double 타입을 String으로 변환하여 저장 (Redis Stream에서 안정적으로 읽기 위함)
        fields.put("startX", String.valueOf(req.getStartX()));
        fields.put("startY", String.valueOf(req.getStartY()));
        fields.put("startName", req.getStartName());
        fields.put("endX", String.valueOf(req.getEndX()));
        fields.put("endY", String.valueOf(req.getEndY()));
        fields.put("endName", req.getEndName());

        RecordId recordId = redisTemplate.opsForStream().add(
                StreamRecords.mapBacked(fields)
                        .withStreamKey(streamKey)
        );

        log.info("Location stream published. stream={}, recordId={}, data={}",
                streamKey, recordId, fields);
    }

    // Redis Stream에서 경로 정보 조회 (보호자가 사용자의 현재 길안내 정보 확인용)
    public Optional<Map<Object, Object>> read(Long relationId) {
        String streamKey = "location-stream:" + relationId;

        try {
            // Stream의 모든 메시지 읽기 (최신 메시지만 필요하므로 XREVRANGE로 최신 1개 조회)
            List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
                    .reverseRange(streamKey, org.springframework.data.domain.Range.unbounded(),
                            org.springframework.data.redis.connection.RedisZSetCommands.Limit.limit().count(1));

            if (records != null && !records.isEmpty()) {
                MapRecord<String, Object, Object> record = records.get(0);
                Map<Object, Object> fields = record.getValue();
                log.info("Location stream read. stream={}, data={}", streamKey, fields);
                return Optional.of(fields);
            } else {
                log.info("Location stream is empty or not found. stream={}", streamKey);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Failed to read location stream. stream={}, error={}", streamKey, e.getMessage());
            return Optional.empty();
        }
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
