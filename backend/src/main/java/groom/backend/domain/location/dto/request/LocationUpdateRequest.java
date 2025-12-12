package groom.backend.domain.location.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record LocationUpdateRequest(

        @NotNull
        Double lat,

        @NotNull
        Double lng,

        @NotNull
        LocalDateTime time
) {
}
