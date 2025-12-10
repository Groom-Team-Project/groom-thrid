package groom.backend.domain.notification.service.spec;

import groom.backend.domain.notification.dto.request.CreateNotificationRequest;
import groom.backend.domain.notification.dto.response.AlertCheckResponse;

public interface NotificationService {

    public void createAlert(Long relationId, CreateNotificationRequest req);

    public AlertCheckResponse alertCheck(Long relationId);
}
