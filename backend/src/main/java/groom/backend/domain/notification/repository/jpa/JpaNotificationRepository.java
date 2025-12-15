package groom.backend.domain.notification.repository.jpa;

import groom.backend.domain.notification.entity.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaNotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * relationId로 가장 최신 알림 조회 (createdAt 기준 내림차순 정렬)
     */
    Optional<Notification> findFirstByRelation_IdOrderByCreatedAtDesc(Long relationId);

    /**
     * relationId로 모든 알림 조회 (createdAt 기준 내림차순 정렬)
     */
    List<Notification> findAllByRelation_IdOrderByCreatedAtDesc(Long relationId);
}
