package groom.backend.domain.notification.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateNotificationRequest(

        @NotNull
        Double lat,

        @NotNull
        Double lng,

        @NotNull
        String address,

        @NotNull
        LocalDateTime time
) {
}
