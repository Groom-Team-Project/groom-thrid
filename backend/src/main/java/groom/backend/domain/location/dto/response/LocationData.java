package groom.backend.domain.location.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record LocationData(

        UUID userId,

        Double lat,

        Double lng,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime time
) {

    public static LocationData of(UUID userId, String userName, Double latitude, Double longitude) {
        return LocationData.builder()
                .userId(userId)
                .lat(latitude)
                .lng(longitude)
                .time(LocalDateTime.now())
                .build();
    }
}
