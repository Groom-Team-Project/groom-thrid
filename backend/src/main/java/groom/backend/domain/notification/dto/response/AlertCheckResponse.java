package groom.backend.domain.notification.dto.response;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AlertCheckResponse(
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
