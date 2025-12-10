package groom.backend.domain.notification.service.spec;

import groom.backend.domain.notification.dto.request.CreateNotificationRequest;

public interface NotificationService {

    public void createAlert(Long relationId, CreateNotificationRequest req);
}
