package groom.backend.domain.notification.service.spec;

import groom.backend.domain.notification.dto.request.CreateNotificationRequest;
import groom.backend.domain.notification.dto.response.AlertCheckResponse;
import java.util.List;

public interface NotificationService {

    void createAlert(Long relationId, CreateNotificationRequest req);

    AlertCheckResponse alertCheck(Long relationId);

    List<AlertCheckResponse> alertList(Long relationId);
}
