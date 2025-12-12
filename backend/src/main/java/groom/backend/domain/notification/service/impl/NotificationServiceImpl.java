package groom.backend.domain.notification.service.impl;

import groom.backend.domain.notification.dto.request.CreateNotificationRequest;
import groom.backend.domain.notification.dto.response.AlertCheckResponse;
import groom.backend.domain.notification.entity.Notification;
import groom.backend.domain.notification.mapper.NotificationMapper;
import groom.backend.domain.notification.repository.spec.NotificationRepository;
import groom.backend.domain.notification.service.spec.NotificationService;
import groom.backend.domain.sse.service.spec.SseService;
import groom.backend.domain.users.entity.UserRelation;
import groom.backend.domain.users.repository.spec.UserRelationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRelationRepository userRelationRepository;
    private final SseService sseService;

    @Override
    public void createAlert(Long relationId, CreateNotificationRequest req) {
        UserRelation userRelation = userRelationRepository.findById(relationId).get();

        Notification notification = NotificationMapper.toEntity(req);
        userRelation.addNotification(notification);

        Notification savedNotification = notificationRepository.save(notification);

        AlertCheckResponse res = NotificationMapper.toDto(savedNotification);

        sseService.send(relationId, res);
    }

    @Override
    public AlertCheckResponse alertCheck(Long relationId) {
        
        Notification latestNotification = notificationRepository
                .findLatestByRelationId(relationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다"));

        return NotificationMapper.toDto(latestNotification);
    }
}
