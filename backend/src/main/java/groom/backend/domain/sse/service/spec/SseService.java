package groom.backend.domain.sse.service.spec;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {

    SseEmitter connect(Long relationId);

    void disconnect(Long relationId);

    boolean isConnect(Long relationId);

    void send(Long relationId, Object data);
}
