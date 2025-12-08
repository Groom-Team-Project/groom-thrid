package groom.backend.domain.sse.controller;

import groom.backend.common.security.AuthUser;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseServiceImpl sseService;

    @PostMapping("/connect")
    public void connect(@AuthenticationPrincipal AuthUser user) {

        UUID userId = user.userId();

        sseService.connect(userId);
    }

}
