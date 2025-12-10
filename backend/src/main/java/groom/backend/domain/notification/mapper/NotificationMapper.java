package groom.backend.domain.notification.mapper;

import groom.backend.domain.notification.dto.request.CreateNotificationRequest;
import groom.backend.domain.notification.dto.response.AlertCheckResponse;
import groom.backend.domain.notification.entity.Notification;

public class NotificationMapper {

    public static Notification toEntity(CreateNotificationRequest req) {

        Notification entity = Notification.createNotification(
                req.lng(),
                req.lng(),
                req.address()
        );

        return entity;
    }

    public static AlertCheckResponse toDto(Notification notification) {
        
        AlertCheckResponse dto = new AlertCheckResponse(
                notification.getLat(),
                notification.getLng(),
                notification.getAddress()
        );

        return dto;
    }
}
