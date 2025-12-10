package groom.backend.domain.notification.service.impl;

import groom.backend.domain.notification.dto.request.CreateNotificationRequest;
import groom.backend.domain.notification.dto.response.AlertCheckResponse;
import groom.backend.domain.notification.service.spec.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Override
    public void createAlert(Long relationId, CreateNotificationRequest req) {

    }

    @Override
    public AlertCheckResponse alertCheck(Long relationId) {
        return null;
    }
}
