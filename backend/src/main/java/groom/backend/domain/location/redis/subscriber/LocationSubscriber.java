package groom.backend.domain.location.redis.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import groom.backend.domain.location.dto.request.LocationUpdateRequest;
import groom.backend.domain.sse.service.spec.SseService;
import groom.backend.domain.users.repository.spec.UserRelationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

// Redis Pub/Sub 위치 정보 Subscriber - 채널: "location:{relationId}"에서 위치 정보 수신 - UserRelation에서 보호자 조회 - SSE를 통해 보호자에게 위치 정보 전송
@Component
@Slf4j
@RequiredArgsConstructor
public class LocationSubscriber implements MessageListener {

    private final SseService sseService;
    private final UserRelationRepository userRelationRepository;
    private final ObjectMapper objectMapper;

    // Redis 메시지 수신 처리 - 메시지: LocationData (JSON) - 채널: location:{relationId}
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {

            String channel = new String(message.getChannel());

            // relationId 추출
            String[] split = channel.split(":");
            Long relationId = Long.parseLong(split[1]);

            // 메시지 본문 파싱 (LocationData JSON -> Object)
            String messageBody = new String(message.getBody());
            LocationUpdateRequest locationData = objectMapper.readValue(messageBody, LocationUpdateRequest.class);

            log.info("위치 정보 수신: relationId={}, lat={}, lng={}, timestamp={}",
                    relationId, locationData.lat(), locationData.lng(), locationData.time());

            // 보호자가 SSE 연결 상태인지 확인
            if (!sseService.isConnect(relationId)) {
                log.info("보호자가 SSE 연결 상태가 아님: protectorId={}", relationId);
                return;
            }

            // 보호자에게 위치 정보 전송
            sseService.send(relationId, locationData);
            log.info("보호자에게 위치 정보 전송 완료: protectorId={}", relationId);

        } catch (Exception e) {
            log.error("위치 정보 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
