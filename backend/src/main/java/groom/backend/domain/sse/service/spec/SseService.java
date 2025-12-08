package groom.backend.domain.sse.service.spec;

import java.util.UUID;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {

    SseEmitter connect(UUID userId);

    void disconnect(UUID userId);

    boolean isConnect(UUID userId);

    void send(Integer relationId, Object data);
}
