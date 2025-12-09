package groom.backend.domain.sse.service.impl;

import groom.backend.common.exception.BusinessException;
import groom.backend.common.exception.ErrorCode;
import groom.backend.domain.sse.service.spec.SseService;
import groom.backend.domain.users.repository.spec.UserRelationRepository;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 연결 관리 서비스 - In-Memory 방식으로 사용자별 SseEmitter 관리 - Redis Pub/Sub을 통해 받은 위치 정보를 보호자에게 전송
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    // SSE 연결 타임아웃: 10분
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 10;

    // 사용자별 SseEmitter 저장소 (Key: relationId, Value: SseEmitter)
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final UserRelationRepository userRelationRepository;

    // SSE 연결 생성 - 새로운 SseEmitter 생성 및 저장 - 타임아웃/에러/완료 시 연결 해제
    @Override
    public SseEmitter connect(Long relationId) {

        if (relationId == null) {
            throw new BusinessException(ErrorCode.RELATION_NOT_FOUND);
        }

        // 기존 연결이 있으면 제거
        if (emitters.containsKey(relationId)) {
            log.info("기존 SSE 연결 제거: relationId={}", relationId);
            emitters.get(relationId).complete();
            emitters.remove(relationId);
        }

        // 새로운 Emitter 생성
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // relationId 기반 생성
        emitters.put(relationId, emitter);
        log.info("SSE 연결 생성: userId={}, timeout={}ms", relationId, DEFAULT_TIMEOUT);

        // 연결 정상 종료시 연결해제 설정
        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료: userId={}", relationId);
            emitters.remove(relationId);
        });

        // 타임아웃 시 제거 설정
        emitter.onTimeout(() -> {
            log.warn("SSE 연결 타임아웃: userId={}", relationId);
            emitters.remove(relationId);
        });

        // 에러 발생 시 제거
        emitter.onError(throwable -> {
            log.error("SSE 연결 에러: userId={}, error={}", relationId, throwable.getMessage());
            emitters.remove(relationId);
        });

        // 연결 확인용 초기 이벤트 전송 설정
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE 연결이 성공적으로 생성되었습니다"));
            log.info("SSE 초기 이벤트 전송 성공: userId={}", relationId);
        } catch (IOException e) {
            log.error("SSE 초기 이벤트 전송 실패: userId={}, error={}", relationId, e.getMessage());
            emitters.remove(relationId);
            throw new RuntimeException("SSE 연결 초기화 실패", e);
        }

        return emitter;
    }

    // SSE 연결 해제 - 명시적으로 연결을 종료할 때 사용
    @Override
    public void disconnect(Long relationId) {

        SseEmitter emitter = emitters.get(relationId);
        if (emitter != null) {
            emitter.complete();
            emitters.remove(relationId);
            log.info("SSE 연결 해제: userId={}", relationId);
        } else {
            log.warn("SSE 연결 해제 실패: 연결이 존재하지 않음, userId={}", relationId);
        }
    }

    // SSE 연결 상태 확인
    @Override
    public boolean isConnect(Long relationId) {

        boolean connected = emitters.containsKey(relationId);
        log.debug("SSE 연결 상태 확인: relationId={}, connected={}", relationId, connected);
        return connected;
    }

    // 특정 사용자에게 데이터 전송 - Redis Pub/Sub Subscriber에서 호출 - 보호자에게 위치 정보 전송
    @Override
    public void send(Long relationId, Object data) {

        SseEmitter emitter = emitters.get(relationId);

        if (emitter == null) {
            log.warn("SSE 전송 실패: 연결이 존재하지 않음, userId={}", relationId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("location")
                    .data(data));
            log.info("SSE 데이터 전송 성공: userId={}", relationId);
        } catch (IOException e) {
            log.error("SSE 데이터 전송 실패: userId={}, error={}", relationId, e.getMessage());
            emitters.remove(relationId);
        }
    }
}
