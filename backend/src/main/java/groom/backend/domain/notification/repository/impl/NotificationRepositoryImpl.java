package groom.backend.domain.notification.repository.impl;

import groom.backend.domain.notification.entity.Notification;
import groom.backend.domain.notification.repository.jpa.JpaNotificationRepository;
import groom.backend.domain.notification.repository.spec.NotificationRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final JpaNotificationRepository jpaNotificationRepository;

    @Override
    public Notification save(Notification notification) {
        return jpaNotificationRepository.save(notification);
    }

    @Override
    public Optional<Notification> findLatestByRelationId(Long relationId) {
        return jpaNotificationRepository.findFirstByRelation_IdOrderByCreatedAtDesc(relationId);
    }
}
