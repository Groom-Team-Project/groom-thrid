package groom.backend.domain.location.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import groom.backend.domain.location.dto.request.LocationUpdateRequest;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record LocationData(

        Long relationId,

        Double lat,

        Double lng,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime time
) {

    public static LocationData of(LocationUpdateRequest req) {
        return LocationData.builder()
                .lat(req.lat())
                .lng(req.lat())
                .time(LocalDateTime.now())
                .build();
    }
}
