package groom.backend.domain.sse.controller;

import groom.backend.common.security.AuthUser;
import groom.backend.domain.sse.service.impl.SseServiceImpl;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/v1/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseServiceImpl sseService;

    @GetMapping("/connect")
    public SseEmitter connect(@AuthenticationPrincipal AuthUser user) {

        UUID userId = user.userId();

        return sseService.connect(userId);
    }

    @PostMapping("/disconnect")
    public void disconnect(@AuthenticationPrincipal AuthUser user) {
        UUID userId = user.userId();

        sseService.disconnect(userId);
    }
}
