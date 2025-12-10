package groom.backend.domain.notification.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateNotificationRequest(

        @NotNull
        Double lat,

        @NotNull
        Double lng,

        @NotNull
        String address
) {
}
