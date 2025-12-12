package groom.backend.domain.notification.repository.spec;

import groom.backend.domain.notification.entity.Notification;
import java.util.Optional;

public interface NotificationRepository {

    Notification save(Notification notification);

    /**
     * relationId로 가장 최신 알림 조회
     */
    Optional<Notification> findLatestByRelationId(Long relationId);
}
