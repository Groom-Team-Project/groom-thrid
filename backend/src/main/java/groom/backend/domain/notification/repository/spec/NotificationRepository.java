package groom.backend.domain.notification.repository.spec;

import groom.backend.domain.notification.entity.Notification;

public interface NotificationRepository {

    void save(Notification notification);

    Notification findById(Long relationId);
}
