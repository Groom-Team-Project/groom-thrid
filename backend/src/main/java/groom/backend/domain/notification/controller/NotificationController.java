package groom.backend.domain.notification.controller;

import groom.backend.common.security.AuthUser;
import groom.backend.domain.notification.dto.request.CreateNotificationRequest;
import groom.backend.domain.notification.dto.response.AlertCheckResponse;
import groom.backend.domain.notification.service.spec.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/alert")
    public void createAlert(@AuthenticationPrincipal AuthUser user, @RequestBody CreateNotificationRequest req) {

        Long relationId = user.relationId();

        notificationService.createAlert(relationId, req);
    }


    @GetMapping("/alert-check")
    public AlertCheckResponse alertCheck(@AuthenticationPrincipal AuthUser user) {

        Long relationId = user.relationId();

        notificationService.alertCheck(relationId);
    }
}
